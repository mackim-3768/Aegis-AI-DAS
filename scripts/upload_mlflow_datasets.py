import argparse
import hashlib
import json
import os
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def _count_lines(path: Path) -> int:
    n = 0
    with path.open("rb") as f:
        for _ in f:
            n += 1
    return n


def _format_compact_count(n: int) -> str:
    if n >= 1_000_000 and n % 1_000_000 == 0:
        return f"{n // 1_000_000}m"
    if n >= 1_000 and n % 1_000 == 0:
        return f"{n // 1_000}k"
    if n >= 1_000_000:
        return f"{n / 1_000_000:.1f}m".rstrip("0").rstrip(".")
    if n >= 1_000:
        return f"{n / 1_000:.1f}k".rstrip("0").rstrip(".")
    return str(n)


def _discover_dataset_files(dataset_dir: Path) -> List[Path]:
    return _discover_dataset_files_with_options(dataset_dir, include_json=False)


def _discover_dataset_files_with_options(dataset_dir: Path, *, include_json: bool) -> List[Path]:
    files: List[Path] = []
    files.extend(sorted(dataset_dir.glob("*.jsonl")))
    if include_json:
        files.extend(sorted(dataset_dir.glob("*.json")))
    return sorted(set(files))


def _split_name(path: Path) -> str:
    return path.stem


def _build_run_name(name_prefix: str, split: str, line_count: Optional[int]) -> str:
    if line_count is None:
        return f"{name_prefix}_{split}"
    return f"{name_prefix}_{split}_{_format_compact_count(line_count)}"


def _read_jsonl_records(path: Path) -> Iterable[Dict[str, object]]:
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            yield json.loads(line)


def _escape_filter_string_value(value: str) -> str:
    return value.replace("'", "\\'")


def _build_dataset_record(sample: Dict[str, object], *, include_tools: bool) -> Dict[str, object]:
    messages = sample.get("messages")
    tools = sample.get("tools")

    if not isinstance(messages, list) or len(messages) < 2:
        raise ValueError("Invalid sample: missing messages")

    dev = messages[0] if len(messages) >= 1 else None
    usr = messages[1] if len(messages) >= 2 else None
    asst = messages[2] if len(messages) >= 3 else None

    def _content(m: object) -> str:
        if not isinstance(m, dict):
            return ""
        c = m.get("content")
        return c if isinstance(c, str) else ""

    expected_response: str = ""
    if isinstance(asst, dict):
        if isinstance(asst.get("content"), str) and asst.get("content"):
            expected_response = str(asst.get("content"))
        elif "tool_calls" in asst:
            expected_response = json.dumps({"tool_calls": asst.get("tool_calls")}, ensure_ascii=False, separators=(",", ":"))

    inputs: Dict[str, object] = {
        "developer": _content(dev),
        "user": _content(usr),
    }
    if include_tools and tools is not None:
        inputs["tools"] = tools

    record: Dict[str, object] = {
        "inputs": inputs,
    }

    if expected_response:
        record["expectations"] = {"expected_response": expected_response}

    md = sample.get("metadata")
    if isinstance(md, str):
        record["tags"] = {"sample_metadata": md}

    return record


def _upload_one_run(
    *,
    mlflow,
    tracking_uri: str,
    experiment_name: str,
    artifact_root: str,
    name_prefix: str,
    dataset_file: Path,
) -> Tuple[str, str]:
    mlflow.set_tracking_uri(tracking_uri)
    mlflow.set_experiment(experiment_name)

    split = _split_name(dataset_file)
    line_count: Optional[int] = None
    if dataset_file.suffix.lower() == ".jsonl":
        line_count = _count_lines(dataset_file)

    run_name = _build_run_name(name_prefix, split, line_count)

    meta: Dict[str, object] = {
        "name_prefix": name_prefix,
        "split": split,
        "file_name": dataset_file.name,
        "relative_path": str(dataset_file.as_posix()),
        "bytes": dataset_file.stat().st_size,
        "sha256": _sha256(dataset_file),
        "created_at": datetime.now().isoformat(timespec="seconds"),
    }
    if line_count is not None:
        meta["lines"] = line_count

    with mlflow.start_run(run_name=run_name) as run:
        run_id = run.info.run_id
        mlflow.set_tag("dataset_name", run_name)
        mlflow.set_tag("dataset_split", split)

        artifact_path = f"{artifact_root}/{run_name}"
        mlflow.log_artifact(str(dataset_file), artifact_path=artifact_path)
        mlflow.log_dict(meta, f"{artifact_path}/metadata.json")

    return run_id, run_name


def _get_or_create_experiment_id(*, client, experiment_name: str) -> str:
    exp = client.get_experiment_by_name(experiment_name)
    if exp is not None:
        return exp.experiment_id
    return client.create_experiment(experiment_name)


def _find_existing_dataset_id(*, client, experiment_id: str, dataset_name: str) -> Optional[str]:
    escaped = _escape_filter_string_value(dataset_name)
    try:
        results = client.search_datasets(
            experiment_ids=[experiment_id],
            filter_string=f"name = '{escaped}'",
            max_results=1,
        )
    except TypeError:
        results = client.search_datasets(
            experiment_ids=[experiment_id],
            filter_string=f"name = '{escaped}'",
            max_results=1,
        )
    if results:
        ds = results[0]
        return getattr(ds, "dataset_id", None)
    return None


def _register_evaluation_dataset(
    *,
    tracking_uri: str,
    experiment_name: str,
    name_prefix: str,
    dataset_file: Path,
    batch_size: int,
    include_tools: bool,
    if_exists: str,
) -> Dict[str, str]:
    import mlflow
    from mlflow.tracking import MlflowClient

    mlflow.set_tracking_uri(tracking_uri)

    client = MlflowClient()
    experiment_id = _get_or_create_experiment_id(client=client, experiment_name=experiment_name)

    split = _split_name(dataset_file)
    line_count: Optional[int] = None
    if dataset_file.suffix.lower() == ".jsonl":
        line_count = _count_lines(dataset_file)

    dataset_name = _build_run_name(name_prefix, split, line_count)

    existing_id = _find_existing_dataset_id(client=client, experiment_id=experiment_id, dataset_name=dataset_name)
    if existing_id is not None:
        if if_exists == "skip":
            return {"dataset_id": existing_id, "dataset_name": dataset_name, "status": "skipped"}
        if if_exists == "merge":
            dataset = client.get_dataset(existing_id)
        elif if_exists == "replace":
            client.delete_dataset(dataset_id=existing_id)
            dataset = None
        else:
            raise RuntimeError(
                f"Dataset already exists: {dataset_name} (dataset_id={existing_id}). "
                f"Use --if-exists merge/replace/skip"
            )
    else:
        dataset = None

    tags: Dict[str, object] = {
        "name_prefix": name_prefix,
        "split": split,
        "file_name": dataset_file.name,
        "bytes": dataset_file.stat().st_size,
        "sha256": _sha256(dataset_file),
        "created_at": datetime.now().isoformat(timespec="seconds"),
    }
    if line_count is not None:
        tags["lines"] = line_count

    if dataset is None:
        dataset = client.create_dataset(name=dataset_name, experiment_id=experiment_id, tags=tags)

    batch: List[Dict[str, object]] = []
    total = 0
    for sample in _read_jsonl_records(dataset_file):
        record = _build_dataset_record(sample, include_tools=include_tools)
        batch.append(record)
        if len(batch) >= batch_size:
            dataset.merge_records(batch)
            total += len(batch)
            batch.clear()

    if batch:
        dataset.merge_records(batch)
        total += len(batch)

    return {"dataset_id": dataset.dataset_id, "dataset_name": dataset_name, "status": "created", "records": str(total)}


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--tracking-uri", default=os.environ.get("MLFLOW_TRACKING_URI", ""))
    p.add_argument("--experiment", default="Aegis-AI-DAS")
    p.add_argument("--dataset-dir", default="DataSet")
    p.add_argument("--name-prefix", default="aegis_fc240m_toolcall_policyV1")
    p.add_argument("--mode", choices=["datasets", "artifacts"], default="datasets")
    p.add_argument("--batch-size", type=int, default=200)
    p.add_argument("--include-tools", action="store_true", default=False)
    p.add_argument("--if-exists", choices=["error", "skip", "merge", "replace"], default="error")
    p.add_argument("--artifact-root", default="datasets")
    p.add_argument("--include-json", action="store_true", default=False)
    p.add_argument("--per-split", action="store_true", default=True)
    args = p.parse_args()

    if not args.tracking_uri:
        print("ERROR: --tracking-uri is required (or set MLFLOW_TRACKING_URI)", file=sys.stderr)
        return 2

    dataset_dir = Path(args.dataset_dir)
    if not dataset_dir.exists() or not dataset_dir.is_dir():
        print(f"ERROR: dataset dir not found: {dataset_dir}", file=sys.stderr)
        return 2

    files = _discover_dataset_files_with_options(dataset_dir, include_json=args.include_json)
    if not files:
        print(f"ERROR: no dataset files found under {dataset_dir}", file=sys.stderr)
        return 2

    if args.mode == "artifacts":
        try:
            import mlflow  # type: ignore
        except Exception as e:
            print("ERROR: failed to import mlflow. Install first: pip install mlflow", file=sys.stderr)
            print(f"DETAIL: {e}", file=sys.stderr)
            return 2

        uploaded: List[Dict[str, str]] = []
        for f in files:
            run_id, run_name = _upload_one_run(
                mlflow=mlflow,
                tracking_uri=args.tracking_uri,
                experiment_name=args.experiment,
                artifact_root=args.artifact_root,
                name_prefix=args.name_prefix,
                dataset_file=f,
            )
            uploaded.append({"file": str(f), "run_id": run_id, "run_name": run_name})
            print(json.dumps(uploaded[-1], ensure_ascii=False))
    else:
        created: List[Dict[str, str]] = []
        for f in files:
            info = _register_evaluation_dataset(
                tracking_uri=args.tracking_uri,
                experiment_name=args.experiment,
                name_prefix=args.name_prefix,
                dataset_file=f,
                batch_size=args.batch_size,
                include_tools=args.include_tools,
                if_exists=args.if_exists,
            )
            info["file"] = str(f)
            created.append(info)
            print(json.dumps(info, ensure_ascii=False))

    summary_path = dataset_dir / "mlflow_upload_summary.json"
    with summary_path.open("w", encoding="utf-8") as out:
        json.dump({"mode": args.mode, "files": [str(f) for f in files]}, out, ensure_ascii=False, indent=2)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
