#!/usr/bin/env python3
import argparse
import json
import os
import random
import re
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Tuple


JSONType = Any


def load_json(path: str) -> JSONType:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def json_dumps_one_line(obj: Any) -> str:
    return json.dumps(obj, ensure_ascii=False, separators=(",", ":"))


class SchemaError(ValueError):
    pass


def _is_int(v: Any) -> bool:
    return isinstance(v, int) and not isinstance(v, bool)


def validate_value(value: Any, schema: Dict[str, Any], path: str) -> None:
    t = schema.get("type")
    if t == "OBJECT":
        if not isinstance(value, dict):
            raise SchemaError(f"{path}: expected OBJECT")
        props: Dict[str, Any] = schema.get("properties", {})
        required: List[str] = schema.get("required", [])
        additional = schema.get("additionalProperties", True)

        for k in required:
            if k not in value:
                raise SchemaError(f"{path}: missing required '{k}'")

        if additional is False:
            extra = [k for k in value.keys() if k not in props]
            if extra:
                raise SchemaError(f"{path}: additionalProperties=false, extra keys {extra}")

        for k, v in value.items():
            if k not in props:
                if additional is False:
                    raise SchemaError(f"{path}: unexpected key '{k}'")
                continue
            validate_value(v, props[k], f"{path}.{k}")
        return

    if t == "ARRAY":
        if not isinstance(value, list):
            raise SchemaError(f"{path}: expected ARRAY")
        item_schema = schema.get("items")
        if item_schema is not None:
            for i, item in enumerate(value):
                validate_value(item, item_schema, f"{path}[{i}]")
        return

    if t == "STRING":
        if not isinstance(value, str):
            raise SchemaError(f"{path}: expected STRING")
        enum = schema.get("enum")
        if enum is not None and value not in enum:
            raise SchemaError(f"{path}: value '{value}' not in enum {enum}")
        pattern = schema.get("pattern")
        if pattern is not None and re.match(pattern, value) is None:
            raise SchemaError(f"{path}: value '{value}' does not match pattern")
        return

    if t == "BOOLEAN":
        if not isinstance(value, bool):
            raise SchemaError(f"{path}: expected BOOLEAN")
        return

    if t == "INTEGER":
        if not _is_int(value):
            raise SchemaError(f"{path}: expected INTEGER")
        minimum = schema.get("minimum")
        maximum = schema.get("maximum")
        if minimum is not None and value < minimum:
            raise SchemaError(f"{path}: {value} < minimum {minimum}")
        if maximum is not None and value > maximum:
            raise SchemaError(f"{path}: {value} > maximum {maximum}")
        return

    if t == "NUMBER":
        if not isinstance(value, (int, float)) or isinstance(value, bool):
            raise SchemaError(f"{path}: expected NUMBER")
        minimum = schema.get("minimum")
        maximum = schema.get("maximum")
        if minimum is not None and value < minimum:
            raise SchemaError(f"{path}: {value} < minimum {minimum}")
        if maximum is not None and value > maximum:
            raise SchemaError(f"{path}: {value} > maximum {maximum}")
        return

    raise SchemaError(f"{path}: unknown schema type {t}")


@dataclass(frozen=True)
class RiskDecision:
    risk_type: str  # one of priority list
    confidence: float
    tier: str  # full|warning|low|none


PRIORITY_ORDER = [
    "forward_collision",
    "vehicle_intrusion",
    "blind_spot",
    "lane_departure",
    "drowsiness",
    "ev_battery_critical",
    "environmental_hazards",
]


def confidence_to_tier(conf: float) -> str:
    if conf >= 0.9:
        return "full"
    if 0.75 <= conf < 0.9:
        return "warning"
    if 0.65 <= conf < 0.75:
        return "low"
    return "none"


def choose_user_format(rng: random.Random) -> str:
    return "sensor_only" if rng.random() < 0.5 else "inquiry_plus_sensor"


def pick_confidence(rng: random.Random, tier: str) -> float:
    if tier == "full":
        return round(rng.uniform(0.9, 1.0), 2)
    if tier == "warning":
        return round(rng.uniform(0.75, 0.89), 2)
    if tier == "low":
        return round(rng.uniform(0.65, 0.74), 2)
    if tier == "none":
        return round(rng.uniform(0.3, 0.64), 2)
    raise ValueError(tier)


def clamp01(x: float) -> float:
    return max(0.0, min(1.0, x))


def gen_forward_collision_context(rng: random.Random, desired: str, conf: float) -> Dict[str, Any]:
    level = desired
    if level not in ("low", "mid", "high"):
        level = "mid"
    score_ranges = {
        "low": (0.05, 0.35),
        "mid": (0.36, 0.75),
        "high": (0.76, 0.99),
    }
    lo, hi = score_ranges[level]
    score = round(rng.uniform(lo, hi), 2)
    return {"score": score, "level": level, "confidence": conf}


def gen_intrusion_context(rng: random.Random, level: str, conf: float) -> Dict[str, Any]:
    if level not in ("low", "mid", "high", "critical"):
        level = "mid"
    value = level in ("high", "critical") or rng.random() < 0.7
    return {"value": bool(value), "level": level, "confidence": conf}


def gen_blind_spot_context(rng: random.Random, level: str, conf: float) -> Dict[str, Any]:
    if level not in ("low", "mid", "high"):
        level = "mid"
    value = True if level in ("mid", "high") else rng.random() < 0.4
    return {"value": bool(value), "level": level, "confidence": conf}


def gen_lane_departure_context(rng: random.Random, value: bool, conf: float) -> Dict[str, Any]:
    return {"value": bool(value), "confidence": conf}


def gen_drowsiness_context(rng: random.Random, value: bool, conf: float) -> Dict[str, Any]:
    return {"value": bool(value), "confidence": conf}


def gen_ev_battery_context(rng: random.Random, level: str) -> Dict[str, Any]:
    # required: temperature, level, cooling_active
    level_to_temp = {
        "normal": (20.0, 40.0),
        "warm": (40.1, 55.0),
        "hot": (55.1, 70.0),
        "critical": (70.1, 90.0),
    }
    if level not in level_to_temp:
        level = "warm"
    lo, hi = level_to_temp[level]
    temp = round(rng.uniform(lo, hi), 1)
    cooling_active = level in ("hot", "critical") or rng.random() < 0.4
    return {"temperature": temp, "level": level, "cooling_active": bool(cooling_active)}


def gen_environment_hazards_context(rng: random.Random, severity: str, conf: float, count: int) -> Dict[str, Any]:
    if severity not in ("low", "mid", "high"):
        severity = "mid"
    kinds = ["debris", "construction", "accident", "other"]
    hazards = []
    for _ in range(max(1, count)):
        hazards.append({"kind": rng.choice(kinds), "severity": severity})
    return {"hazards": hazards, "confidence": conf}


def gen_driving_environment(rng: random.Random) -> Dict[str, Any]:
    weather = rng.choice(["clear", "rain", "snow", "fog", "unknown"])
    road_condition = rng.choice(["dry", "wet", "icy", "unknown"])
    visibility_level = rng.choice(["good", "moderate", "poor", "unknown"])
    return {"weather": weather, "road_condition": road_condition, "visibility_level": visibility_level}


def gen_sensor_health(rng: random.Random, ok: bool) -> Dict[str, Any]:
    if ok:
        return {"overall_ok": True, "camera_ok": True, "model_ok": True}
    # introduce some failures while staying schema-valid
    camera_ok = rng.random() > 0.5
    model_ok = rng.random() > 0.5
    overall_ok = camera_ok and model_ok
    return {"overall_ok": bool(overall_ok), "camera_ok": bool(camera_ok), "model_ok": bool(model_ok)}


def decide_primary_risk(sensor_context: Dict[str, Any]) -> Optional[RiskDecision]:
    candidates: List[Tuple[int, RiskDecision]] = []

    if "get_forward_collision_risk" in sensor_context:
        c = sensor_context["get_forward_collision_risk"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        # treat mid/high as actionable risk
        if c.get("level") in ("mid", "high"):
            candidates.append((0, RiskDecision("forward_collision", conf, tier)))

    if "get_vehicle_system_intrusion_status" in sensor_context:
        c = sensor_context["get_vehicle_system_intrusion_status"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        if c.get("value") is True and c.get("level") in ("high", "critical", "mid"):
            candidates.append((1, RiskDecision("vehicle_intrusion", conf, tier)))

    if "get_blind_spot_collision_risk" in sensor_context:
        c = sensor_context["get_blind_spot_collision_risk"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        if c.get("value") is True and c.get("level") in ("mid", "high"):
            candidates.append((2, RiskDecision("blind_spot", conf, tier)))

    if "get_lane_departure_status" in sensor_context:
        c = sensor_context["get_lane_departure_status"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        if c.get("value") is True:
            candidates.append((3, RiskDecision("lane_departure", conf, tier)))

    if "get_driver_drowsiness_status" in sensor_context:
        c = sensor_context["get_driver_drowsiness_status"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        if c.get("value") is True:
            candidates.append((4, RiskDecision("drowsiness", conf, tier)))

    if "get_ev_battery_thermal_status" in sensor_context:
        c = sensor_context["get_ev_battery_thermal_status"]
        level = c.get("level")
        # no confidence in schema; default to 1.0
        conf = 1.0
        tier = confidence_to_tier(conf)
        if level in ("hot", "critical"):
            candidates.append((5, RiskDecision("ev_battery_critical", conf, tier)))

    if "get_external_environmental_hazards" in sensor_context:
        c = sensor_context["get_external_environmental_hazards"]
        conf = float(c.get("confidence", 1.0))
        tier = confidence_to_tier(conf)
        hazards = c.get("hazards", [])
        if isinstance(hazards, list) and any(h.get("severity") in ("mid", "high") for h in hazards if isinstance(h, dict)):
            candidates.append((6, RiskDecision("environmental_hazards", conf, tier)))

    if not candidates:
        return None
    candidates.sort(key=lambda x: x[0])
    return candidates[0][1]


def build_tool_calls(
    rng: random.Random,
    action_schemas: Dict[str, Dict[str, Any]],
    decision: RiskDecision,
    sensor_context: Dict[str, Any],
) -> List[Dict[str, Any]]:
    tool_calls: List[Dict[str, Any]] = []

    def add_call(name: str, args: Dict[str, Any]) -> None:
        # validate against action schema params
        schema = action_schemas[name]["parameters"]
        validate_value(args, schema, f"action:{name}")
        tool_calls.append({"function": {"name": name, "arguments": args}})

    tier = decision.tier
    rt = decision.risk_type

    if rt == "forward_collision":
        level = sensor_context["get_forward_collision_risk"]["level"]
        if tier == "full" and level == "high":
            add_call("pre_tension_safety_belts", {"enabled": True, "level": "high"})
            add_call("trigger_hud_warning", {"message": rng.choice(["전방 충돌 위험! 즉시 감속하세요", "전방 위험! 제동 준비", "전방 추돌 위험"]), "level": "danger"})
        elif tier in ("full", "warning"):
            severity = "danger" if level == "high" else "warning"
            add_call("trigger_hud_warning", {"message": rng.choice(["전방 위험. 감속하세요", "전방 충돌 위험 감지", "전방 상황 주의"]), "level": severity})
        elif tier == "low":
            add_call("trigger_cluster_visual_warning", {"message": rng.choice(["전방 상황 주의", "전방 위험 가능성" ]), "level": "info"})
        else:
            return []

    elif rt == "vehicle_intrusion":
        ctx = sensor_context["get_vehicle_system_intrusion_status"]
        lvl = ctx["level"]
        if tier == "full" and lvl in ("high", "critical"):
            add_call("request_safe_mode", {"enabled": True, "reason": "vehicle_system_intrusion"})
            add_call("log_safety_event", {"event_type": "vehicle_system_intrusion", "message": "intrusion suspected", "level": "danger"})
        elif tier in ("warning", "full"):
            add_call("log_safety_event", {"event_type": "vehicle_system_intrusion", "message": "intrusion suspected", "level": "warning"})
        elif tier == "low":
            add_call("log_safety_event", {"event_type": "vehicle_system_intrusion", "message": "intrusion low confidence", "level": "info"})
        else:
            return []

    elif rt == "blind_spot":
        ctx = sensor_context["get_blind_spot_collision_risk"]
        lvl = ctx["level"]
        if tier in ("full", "warning"):
            vib_level = "high" if (tier == "full" and lvl == "high") else "mid"
            add_call("trigger_steering_vibration", {"level": vib_level, "duration_ms": rng.choice([800, 1200, 1500, 2000])})
            add_call("trigger_hud_warning", {"message": rng.choice(["사각지대 차량 감지", "사각지대 위험. 차선 변경 주의"]), "level": "warning"})
        elif tier == "low":
            add_call("trigger_steering_vibration", {"level": "low", "duration_ms": rng.choice([600, 800, 1000])})
        else:
            return []

    elif rt == "lane_departure":
        if tier in ("full", "warning"):
            add_call("trigger_steering_vibration", {"level": "mid" if tier == "warning" else "high", "duration_ms": rng.choice([600, 900, 1200])})
        elif tier == "low":
            add_call("trigger_steering_vibration", {"level": "low", "duration_ms": rng.choice([500, 700, 900])})
        else:
            return []

    elif rt == "drowsiness":
        if tier == "full":
            add_call("trigger_drowsiness_alert_sound", {"enabled": True, "level": "high"})
            add_call("trigger_rest_recommendation", {"reason": rng.choice(["졸음 감지", "주의력 저하", "운전 피로 누적"]), "level": "high"})
        elif tier == "warning":
            add_call("trigger_drowsiness_alert_sound", {"enabled": True, "level": "mid"})
        elif tier == "low":
            add_call("trigger_drowsiness_alert_sound", {"enabled": True, "level": "low"})
        else:
            return []

    elif rt == "ev_battery_critical":
        level = sensor_context["get_ev_battery_thermal_status"]["level"]
        if level == "critical":
            add_call("trigger_cluster_visual_warning", {"message": "배터리 열 상태 위험", "level": "danger"})
            add_call("request_safe_mode", {"enabled": True, "reason": "ev_battery_thermal_critical"})
        else:
            add_call("trigger_cluster_visual_warning", {"message": "배터리 온도 상승", "level": "warning"})

    elif rt == "environmental_hazards":
        if tier in ("full", "warning"):
            add_call("activate_hazard_warning_signals", {"enabled": True, "duration_ms": rng.choice([2000, 3000, 5000, 8000])})
        elif tier == "low":
            add_call("trigger_navigation_notification", {"message": "전방 환경 위험 가능성", "level": "info"})
        else:
            return []

    # enforce max 2 tool calls
    return tool_calls[:2]


def build_user_message(rng: random.Random, fmt: str, inquiry: str, sensor_context: Dict[str, Any]) -> str:
    sensor_json = json_dumps_one_line(sensor_context)
    sensor_part = f"[Sensor Context] SENSOR_CONTEXT={sensor_json}"
    if fmt == "sensor_only":
        return sensor_part
    return f"[User Inquiry] {inquiry}\\n{sensor_part}"


def build_developer_message(rng: random.Random) -> str:
    variants = [
        "You are a function calling model. Use the provided tools only when needed.",
        "You can call tools to respond. If no tool is needed, respond normally.",
        "Follow the tool schemas strictly. Do not invent tools.",
    ]
    return rng.choice(variants)


def build_normal_reply(rng: random.Random) -> str:
    return rng.choice(
        [
            "현재 상태로는 즉각적인 조치는 필요 없어 보입니다. 계속 주의 운전하세요.",
            "지금은 위험 신호가 뚜렷하지 않습니다. 상황이 바뀌면 알려주세요.",
            "현재 센서 기준으로는 경고가 필요하지 않습니다.",
        ]
    )


def build_clarification_reply(rng: random.Random, risk_hint: str) -> str:
    return rng.choice(
        [
            f"센서 신뢰도가 낮아 {risk_hint} 여부를 확정하기 어렵습니다. 주변 상황을 한 번 더 확인해 주시겠습니까?",
            f"현재 데이터가 불확실합니다. {risk_hint} 관련 추가 정보(차량 위치/주변 차량/전방 상황)를 제공해 주세요.",
            f"신뢰도가 낮습니다. {risk_hint} 상황이 맞는지 확인이 필요합니다.",
        ]
    )


def build_general_conversation_reply(rng: random.Random) -> str:
    return rng.choice(
        [
            "네, 무엇을 도와드릴까요?",
            "말씀해 주세요. 현재 주행 상태도 함께 확인하겠습니다.",
            "알겠습니다. 질문을 이어서 해주세요.",
        ]
    )


def generate_sample(
    rng: random.Random,
    tools_payload: List[Dict[str, Any]],
    context_schemas: Dict[str, Dict[str, Any]],
    action_schemas: Dict[str, Dict[str, Any]],
    bucket: str,
    for_eval: Optional[str] = None,
) -> Dict[str, Any]:
    fmt = choose_user_format(rng)

    sensor_context: Dict[str, Any] = {}

    sensor_context["get_vehicle_speed"] = {"value": round(rng.uniform(0, 130), 1)}
    if rng.random() < 0.5:
        sensor_context["get_driving_environment"] = gen_driving_environment(rng)
    if rng.random() < 0.35:
        sensor_context["get_sensor_health_status"] = gen_sensor_health(rng, ok=(rng.random() < 0.9))

    inquiry = ""

    if bucket in ("single_action", "multi_action"):
        risk_choices = [
            "forward_collision",
            "vehicle_intrusion",
            "blind_spot",
            "lane_departure",
            "drowsiness",
            "ev_battery_critical",
            "environmental_hazards",
        ]
        weights = [0.22, 0.14, 0.14, 0.14, 0.16, 0.1, 0.1]
        primary = rng.choices(risk_choices, weights=weights, k=1)[0]

        tier = rng.choices(["full", "warning", "low"], weights=[0.45, 0.4, 0.15], k=1)[0]
        conf = pick_confidence(rng, tier)

        if primary == "forward_collision":
            sensor_context["get_forward_collision_risk"] = gen_forward_collision_context(rng, desired=rng.choice(["mid", "high"]), conf=conf)
            inquiry = rng.choice(["앞차와 너무 가까워요", "전방 위험 경고가 필요해요", "전방 추돌 위험이 있는지 확인해줘"])
        elif primary == "vehicle_intrusion":
            sensor_context["get_vehicle_system_intrusion_status"] = gen_intrusion_context(rng, level=rng.choice(["mid", "high", "critical"]), conf=conf)
            inquiry = rng.choice(["차량 시스템에 이상이 있는 것 같아요", "보안 침입 경고가 필요해요", "네트워크 침입 가능성이 있나요?"])
        elif primary == "blind_spot":
            sensor_context["get_blind_spot_collision_risk"] = gen_blind_spot_context(rng, level=rng.choice(["mid", "high"]), conf=conf)
            inquiry = rng.choice(["차선 변경하려는데 옆이 위험해요", "사각지대에 차량이 있나요?", "옆 차가 너무 가까워요"])
        elif primary == "lane_departure":
            sensor_context["get_lane_departure_status"] = gen_lane_departure_context(rng, value=True, conf=conf)
            inquiry = rng.choice(["차선 이탈 경고 해줘", "차선에서 벗어나는 것 같아", "차선 유지가 어려워"])
        elif primary == "drowsiness":
            sensor_context["get_driver_drowsiness_status"] = gen_drowsiness_context(rng, value=True, conf=conf)
            inquiry = rng.choice(["졸음이 오는 것 같아", "졸음 경고 좀 해줘", "집중이 잘 안 돼"])
        elif primary == "ev_battery_critical":
            # no confidence field; map from level
            level = rng.choice(["hot", "critical"]) if tier != "low" else "hot"
            sensor_context["get_ev_battery_thermal_status"] = gen_ev_battery_context(rng, level=level)
            inquiry = rng.choice(["배터리 온도가 높은가요?", "배터리 열 상태가 위험해요", "배터리 경고가 떠요"])
        else:
            severity = rng.choice(["mid", "high"])
            sensor_context["get_external_environmental_hazards"] = gen_environment_hazards_context(rng, severity=severity, conf=conf, count=rng.choice([1, 2, 3]))
            inquiry = rng.choice(["전방 도로에 장애물이 있어요", "낙하물 위험이 있나요?", "공사 구간이 감지됐나요?"])

        if bucket == "multi_action":
            # add 1-2 additional contexts, lower-priority or noise, to create combined context
            extra_count = rng.choice([1, 2])
            extra_pool = [
                "lane_departure",
                "drowsiness",
                "blind_spot",
                "vehicle_intrusion",
                "environmental_hazards",
            ]
            rng.shuffle(extra_pool)
            for extra in extra_pool[:extra_count]:
                if extra == primary:
                    continue
                extra_tier = rng.choices(["full", "warning", "low", "none"], weights=[0.2, 0.35, 0.25, 0.2], k=1)[0]
                extra_conf = pick_confidence(rng, extra_tier)
                if extra == "lane_departure" and "get_lane_departure_status" not in sensor_context:
                    sensor_context["get_lane_departure_status"] = gen_lane_departure_context(rng, value=rng.random() < 0.7, conf=extra_conf)
                elif extra == "drowsiness" and "get_driver_drowsiness_status" not in sensor_context:
                    sensor_context["get_driver_drowsiness_status"] = gen_drowsiness_context(rng, value=rng.random() < 0.6, conf=extra_conf)
                elif extra == "blind_spot" and "get_blind_spot_collision_risk" not in sensor_context:
                    sensor_context["get_blind_spot_collision_risk"] = gen_blind_spot_context(rng, level=rng.choice(["low", "mid", "high"]), conf=extra_conf)
                elif extra == "vehicle_intrusion" and "get_vehicle_system_intrusion_status" not in sensor_context:
                    sensor_context["get_vehicle_system_intrusion_status"] = gen_intrusion_context(rng, level=rng.choice(["low", "mid", "high", "critical"]), conf=extra_conf)
                elif extra == "environmental_hazards" and "get_external_environmental_hazards" not in sensor_context:
                    sensor_context["get_external_environmental_hazards"] = gen_environment_hazards_context(rng, severity=rng.choice(["low", "mid", "high"]), conf=extra_conf, count=rng.choice([1, 2]))

    elif bucket == "no_action":
        # Ensure low-risk contexts
        if rng.random() < 0.6:
            sensor_context["get_forward_collision_risk"] = gen_forward_collision_context(rng, desired="low", conf=round(rng.uniform(0.85, 1.0), 2))
        if rng.random() < 0.5:
            sensor_context["get_lane_departure_status"] = gen_lane_departure_context(rng, value=False, conf=round(rng.uniform(0.8, 1.0), 2))
        if rng.random() < 0.4:
            sensor_context["get_driver_drowsiness_status"] = gen_drowsiness_context(rng, value=False, conf=round(rng.uniform(0.8, 1.0), 2))
        inquiry = rng.choice(["현재 위험이 있는지 알려줘", "지금 상태 괜찮아?", "경고가 필요한 상황인가?"])

    elif bucket == "low_confidence":
        target = rng.choice(["forward_collision", "blind_spot", "lane_departure", "drowsiness", "vehicle_intrusion", "environmental_hazards"])
        conf = pick_confidence(rng, "none")
        if target == "forward_collision":
            sensor_context["get_forward_collision_risk"] = gen_forward_collision_context(rng, desired=rng.choice(["mid", "high"]), conf=conf)
            inquiry = rng.choice(["전방이 위험한가요?", "앞차랑 가까운 것 같은데 확실해?", "전방 위험 판단해줘"])
        elif target == "blind_spot":
            sensor_context["get_blind_spot_collision_risk"] = gen_blind_spot_context(rng, level=rng.choice(["mid", "high"]), conf=conf)
            inquiry = rng.choice(["사각지대에 차량이 있는지 애매해요", "옆차가 있는지 잘 모르겠어", "차선 변경해도 될까?"])
        elif target == "lane_departure":
            sensor_context["get_lane_departure_status"] = gen_lane_departure_context(rng, value=rng.random() < 0.7, conf=conf)
            inquiry = rng.choice(["차선 이탈인가요?", "차선이 잘 안 보여요", "차선 유지 상태가 불확실해"])
        elif target == "drowsiness":
            sensor_context["get_driver_drowsiness_status"] = gen_drowsiness_context(rng, value=rng.random() < 0.7, conf=conf)
            inquiry = rng.choice(["졸음 상태인지 애매해요", "졸음 감지가 불확실해", "졸음 경고가 필요한가?"])
        elif target == "vehicle_intrusion":
            sensor_context["get_vehicle_system_intrusion_status"] = gen_intrusion_context(rng, level=rng.choice(["mid", "high"]), conf=conf)
            inquiry = rng.choice(["시스템 침입 경고가 맞나요?", "보안 위험이 있는지 확실치 않아", "네트워크 이상이 있나요?"])
        else:
            sensor_context["get_external_environmental_hazards"] = gen_environment_hazards_context(rng, severity=rng.choice(["mid", "high"]), conf=conf, count=rng.choice([1, 2]))
            inquiry = rng.choice(["전방 장애물 감지가 불확실해요", "도로 상황이 애매해", "환경 위험이 있는지 알려줘"])

    elif bucket == "general_conversation":
        if rng.random() < 0.5:
            sensor_context["get_forward_collision_risk"] = gen_forward_collision_context(rng, desired="low", conf=round(rng.uniform(0.8, 1.0), 2))
        inquiry = rng.choice(["오늘 운전 팁 알려줘", "지금 내 차 상태 어때?", "피곤할 때 운전은 어떻게 해야 해?"])

    if for_eval == "eval_b":
        if rng.random() < 0.6:
            sensor_context["get_sensor_health_status"] = gen_sensor_health(rng, ok=False)
        if rng.random() < 0.5:
            sensor_context["get_driving_environment"] = {"weather": rng.choice(["rain", "snow", "fog"]), "road_condition": rng.choice(["wet", "icy"]), "visibility_level": rng.choice(["moderate", "poor"])}

    for tool_name, params in sensor_context.items():
        if tool_name not in context_schemas:
            raise SchemaError(f"Unknown context tool '{tool_name}'")
        validate_value(params, context_schemas[tool_name]["parameters"], f"context:{tool_name}")

    user_message = build_user_message(rng, fmt, inquiry, sensor_context)

    developer_message = build_developer_message(rng)

    assistant: Dict[str, Any]

    decision = decide_primary_risk(sensor_context)

    if bucket in ("single_action", "multi_action") and decision is not None and decision.tier != "none":
        tool_calls = build_tool_calls(rng, action_schemas, decision, sensor_context)
        # If we somehow produced 0 tool_calls, treat as normal
        if tool_calls:
            assistant = {"role": "assistant", "content": "", "tool_calls": tool_calls}
        else:
            assistant = {"role": "assistant", "content": build_normal_reply(rng)}
    elif bucket == "low_confidence":
        hint = decision.risk_type if decision is not None else "위험"
        assistant = {"role": "assistant", "content": build_clarification_reply(rng, hint)}
    elif bucket == "general_conversation":
        assistant = {"role": "assistant", "content": build_general_conversation_reply(rng)}
    else:
        assistant = {"role": "assistant", "content": build_normal_reply(rng)}

    sample = {
        "metadata": "eval" if for_eval else "train",
        "tools": tools_payload,
        "messages": [
            {"role": "developer", "content": developer_message},
            {"role": "user", "content": user_message},
            assistant,
        ],
    }

    if set(sample.keys()) != {"metadata", "tools", "messages"}:
        raise SchemaError("Top-level keys mismatch")

    return sample


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--train", type=int, default=12000)
    parser.add_argument("--eval-a", type=int, default=1000)
    parser.add_argument("--eval-b", type=int, default=1000)
    parser.add_argument("--out-dir", type=str, default="DataSet")
    parser.add_argument("--max-tries", type=int, default=30)
    args = parser.parse_args()

    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    tool_schema_dir = os.path.join(repo_root, "ToolSchema")

    context_tool_schema_path = os.path.join(tool_schema_dir, "Context_tool_schema.json")
    action_tool_schema_path = os.path.join(tool_schema_dir, "Action_tool_schema.json")

    context_tools = load_json(context_tool_schema_path)
    action_tools = load_json(action_tool_schema_path)

    # Build schema maps
    context_schemas: Dict[str, Dict[str, Any]] = {}
    for t in context_tools:
        fn = t.get("function", {})
        context_schemas[fn["name"]] = fn

    action_schemas: Dict[str, Dict[str, Any]] = {}
    for t in action_tools:
        fn = t.get("function", {})
        action_schemas[fn["name"]] = fn

    # Tools payload for each sample: include all action tool definitions
    tools_payload = action_tools

    out_dir = os.path.join(repo_root, args.out_dir)
    os.makedirs(out_dir, exist_ok=True)

    rng = random.Random(args.seed)

    def generate_file(path: str, count: int, kind: str, for_eval: Optional[str]) -> Dict[str, int]:
        stats = {
            "single_action": 0,
            "multi_action": 0,
            "no_action": 0,
            "low_confidence": 0,
            "general_conversation": 0,
        }

        # Strict: keep the same batch composition for train/eval files
        buckets = [
            ("single_action", 0.40),
            ("multi_action", 0.30),
            ("no_action", 0.15),
            ("low_confidence", 0.10),
            ("general_conversation", 0.05),
        ]

        # exact counts via rounding then adjust
        bucket_counts = {name: int(round(count * w)) for name, w in buckets}
        # fix rounding drift
        drift = count - sum(bucket_counts.values())
        names = [n for n, _ in buckets]
        i = 0
        while drift != 0:
            n = names[i % len(names)]
            if drift > 0:
                bucket_counts[n] += 1
                drift -= 1
            else:
                if bucket_counts[n] > 0:
                    bucket_counts[n] -= 1
                    drift += 1
            i += 1

        bucket_list: List[str] = []
        for name in names:
            bucket_list.extend([name] * bucket_counts[name])
        rng.shuffle(bucket_list)

        with open(path, "w", encoding="utf-8") as f:
            for bucket in bucket_list:
                tries = 0
                while True:
                    tries += 1
                    if tries > args.max_tries:
                        raise RuntimeError(f"Failed to generate valid sample after {args.max_tries} tries (bucket={bucket})")
                    try:
                        sample = generate_sample(rng, tools_payload, context_schemas, action_schemas, bucket=bucket, for_eval=for_eval)
                        # final JSONL line must be single line
                        line = json_dumps_one_line(sample)
                        if "\n" in line or "\r" in line:
                            raise SchemaError("JSONL line contains newline")
                        f.write(line + "\n")
                        stats[bucket] += 1
                        break
                    except SchemaError:
                        continue
        return stats

    train_path = os.path.join(out_dir, "train.jsonl")
    eval_a_path = os.path.join(out_dir, "eval_a.jsonl")
    eval_b_path = os.path.join(out_dir, "eval_b.jsonl")

    train_stats = generate_file(train_path, args.train, "train", None)
    eval_a_stats = generate_file(eval_a_path, args.eval_a, "eval_a", "eval_a")
    eval_b_stats = generate_file(eval_b_path, args.eval_b, "eval_b", "eval_b")

    summary = {
        "train": train_stats,
        "eval_a": eval_a_stats,
        "eval_b": eval_b_stats,
    }

    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
