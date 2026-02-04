## 🛠️ Aegis-AI : DAS 개발 로드맵 (AI-Driven)

이 문서는 **AI에게 개발을 “위임”하기 위한 작업 지시서**입니다.
각 단계는 반드시 아래 4가지를 포함합니다.

1. **참고(Reference)**: 반드시 읽고 따라야 하는 기존 파일(실제 경로)
2. **작업 대상(Target)**: 이번 단계에서 생성/수정할 파일(구체 경로)
3. **AI 작업 프롬프트(Prompt)**: 그대로 복사해서 AI에게 시킬 수 있는 지시문
4. **완료 기준(Definition of Done)**: 사람이 확인하는 체크리스트

---

### 리포지토리 기준 참고 자료(소스 오브 트루스)

- **프로젝트 개요/탭 구조/개발 규칙**
  - `Docs/01_프로젝트 개요 및 기술 스택 문서.md`
- **기능 명세(탭별 요구사항, Layered View 정책)**
  - `Docs/02_기능 명세서.md`
- **디자인 토큰/모션 정책(Pristine Lab)**
  - `Docs/03_UI 디자인 시스템 가이드.md`
- **도구(50개) 스키마/트리거/UI 전략(가장 중요)**
  - `Docs/04_도구 마스터 레지스트리.md`
  - `Docs/ref/Tool_Defined.md`
- **도구 스키마(JSON) 최종 기준**
  - `ToolSchema/Context_tool_schema.json`
  - `ToolSchema/Action_tool_schema.json`
- **(선택) 데이터셋/템플릿(모델/도구 호출 포맷 참고용)**
  - `DataSet/Example_DataSet-Schema.json`
  - `Docs/ref/dataset_templete.jsonl`

주의:
- 현재 리포에는 **Android/Gradle 프로젝트 파일이 없습니다**. 따라서 0단계에서 스캐폴딩을 먼저 생성합니다.
- `Docs/05_시나리오 및 상태 전이 로직.md`는 현재 비어 있으므로(파일 존재) 2단계에서 채웁니다.

---

## 0단계: Android 프로젝트 스캐폴딩 생성 (Scaffolding)

**목표**: Jetpack Compose 기반 앱 골격(4탭)과 테마(Pristine Lab)까지 “빌드/실행” 가능 상태로 만듭니다.

### 참고(Reference)

- `Docs/01_프로젝트 개요 및 기술 스택 문서.md`
- `Docs/03_UI 디자인 시스템 가이드.md`

### 작업 대상(Target)

아래 파일들은 현재 리포에 없으므로 **신규 생성 대상**입니다(경로는 표준 Android 구조 기준).
패키지명은 생성된 프로젝트의 실제 package에 맞게 일관되게 적용합니다.

- `settings.gradle` 또는 `settings.gradle.kts`
- `build.gradle` 또는 `build.gradle.kts`
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`
- `app/build.gradle` 또는 `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/<package>/MainActivity.kt`
- `app/src/main/java/<package>/ui/theme/Color.kt`
- `app/src/main/java/<package>/ui/theme/Theme.kt`
- `app/src/main/java/<package>/ui/theme/Shape.kt`
- `app/src/main/java/<package>/ui/AppRoot.kt` (4탭 Scaffold + Nav/State 연결 시작점)
- `app/src/main/java/<package>/ui/screens/` (비어 있어도 폴더 생성)

### AI 작업 프롬프트(Prompt)

"""
다음 요구사항으로 Android(Compose) 프로젝트 스캐폴딩을 만들어주세요.

- 참고 문서:
  - Docs/01_프로젝트 개요 및 기술 스택 문서.md
  - Docs/03_UI 디자인 시스템 가이드.md

- 목표:
  - 앱 실행 시 4개 탭(Tab1~Tab4) 가진 기본 UI가 뜨고, 각 탭은 임시 화면(placeholder)이라도 구성
  - Pristine Lab 테마 적용(Color/Shape/Theme 분리)
  - 파일 분리 규칙: 탭별 Composable은 별도 파일로 분리

- 산출물(생성할 파일):
  - app/src/main/java/<package>/MainActivity.kt
  - app/src/main/java/<package>/ui/AppRoot.kt
  - app/src/main/java/<package>/ui/theme/Color.kt, Theme.kt, Shape.kt
  - app/src/main/java/<package>/ui/screens/tab1/Tab1Screen.kt
  - app/src/main/java/<package>/ui/screens/tab2/Tab2Screen.kt
  - app/src/main/java/<package>/ui/screens/tab3/Tab3Screen.kt
  - app/src/main/java/<package>/ui/screens/tab4/Tab4Screen.kt
"""

### 완료 기준(Definition of Done)

- Android Studio에서 Sync 성공
- 앱 실행 시 4개 탭이 정상 렌더링
- 테마 토큰(배경/서피스/포인트 컬러, 라운드 코너)이 적용되어 “화이트 랩(Pristine Lab)” 느낌이 나는지 확인

---

## 1단계: 상태/도구 스키마 기반 “단일 진실 공급원” 구축 (Foundation)

**목표**: 50개 도구(Context 25 + Action 25)를 ViewModel의 단일 상태로 관리하고, 탭들이 이를 구독하도록 만듭니다.

### 참고(Reference)

- `Docs/01_프로젝트 개요 및 기술 스택 문서.md` (Single Source of Truth)
- `Docs/04_도구 마스터 레지스트리.md` (스키마 해석 규칙/엄격성 정책)
- `ToolSchema/Context_tool_schema.json`
- `ToolSchema/Action_tool_schema.json`
- `Docs/ref/Tool_Defined.md`

### 작업 대상(Target)

아래 파일들은 **신규 생성 대상**(또는 생성된 프로젝트에 맞게 경로 조정)입니다.

- `app/src/main/java/<package>/domain/tools/ToolKind.kt` (CONTEXT/ACTION)
- `app/src/main/java/<package>/domain/tools/ToolId.kt` (50개 도구 name 상수)
- `app/src/main/java/<package>/domain/state/ToolState.kt` (50개 도구 상태를 담는 모델)
- `app/src/main/java/<package>/domain/state/AppState.kt` (ToolState + UI 전역 상태: debugMode, processorSelector 등)
- `app/src/main/java/<package>/ui/MainViewModel.kt`

권장 규칙:
- Context Tool은 “상태 반환 스키마”이므로 UI에서 값을 보여주는 모델로 취급합니다.
- Action Tool은 “호출 파라미터 스키마”이므로, UI에서는 “가장 최근 호출 값/활성 상태”를 상태로 보관합니다.

### AI 작업 프롬프트(Prompt)

"""
다음 파일들을 생성/수정해서 50개 도구 상태를 단일 StateFlow로 관리하게 해주세요.

- 참고(반드시 읽고 반영):
  - Docs/04_도구 마스터 레지스트리.md 의 '스키마 해석 규칙(중요)' / '스키마 검증/엄격성 정책'
  - ToolSchema/Context_tool_schema.json
  - ToolSchema/Action_tool_schema.json

- 목표:
  - MainViewModel 내부에 MutableStateFlow<AppState>를 두고, 모든 탭 UI는 이를 collect
  - 50개 도구에 대해 기본값 초기화 제공
  - 업데이트 API를 표준화: updateContextTool(name, partial), applyActionCall(name, args), setDebugMode(bool), setProcessor(type)

- 산출물:
  - app/src/main/java/<package>/domain/state/AppState.kt
  - app/src/main/java/<package>/domain/state/ToolState.kt
  - app/src/main/java/<package>/ui/MainViewModel.kt
"""

### 완료 기준(Definition of Done)

- 앱 전역에서 ViewModel이 1개이고(단일), 모든 탭에서 동일 StateFlow를 구독
- debugMode/processorSelector 같은 전역 설정이 AppState에 포함
- 이후 단계에서 “시나리오 주입/그리드 편집/로그 스트리밍”을 붙일 수 있는 형태로 API 정리

---

## 2단계: 시나리오 인젝터 + 디버그 편집 모드 (Tab 4 중심)

**목표**: 사용자가 시나리오(폭우/졸음 등) 버튼을 누르면 Context 값이 일괄 적용되고, 디버그 모드가 전 탭에 영향을 주도록 합니다.

### 참고(Reference)

- `Docs/02_기능 명세서.md` (Tab 4 요구사항 / 실행 순서)
- `Docs/04_도구 마스터 레지스트리.md` (각 도구 트리거 기준)
- `Docs/05_시나리오 및 상태 전이 로직.md` (이 단계에서 채워야 하는 문서)

### 작업 대상(Target)

- `Docs/05_시나리오 및 상태 전이 로직.md` (비어있는 문서 채우기: 시나리오 정의/적용 규칙)
- `app/src/main/java/<package>/domain/scenario/ScenarioId.kt`
- `app/src/main/java/<package>/domain/scenario/ScenarioPreset.kt` (각 시나리오별 ToolState 덮어쓰기 값)
- `app/src/main/java/<package>/ui/screens/tab4/SystemControlScreen.kt`
- `app/src/main/java/<package>/ui/screens/tab4/components/ScenarioButton.kt`
- `app/src/main/java/<package>/ui/MainViewModel.kt` (applyScenario(preset) 추가)

### AI 작업 프롬프트(Prompt)

"""
Tab4(System Control)에 시나리오 인젝터와 Debug Mode 토글을 구현해주세요.

- 참고:
  - Docs/02_기능 명세서.md 의 4.1/4.2
  - Docs/05_시나리오 및 상태 전이 로직.md (없으니 먼저 이 문서에 시나리오 목록/각 시나리오가 바꾸는 Context Tool을 표로 작성)

- 목표:
  - Debug Mode 토글 -> AppState.debugMode 갱신
  - Processor Selector(CPU/GPU/NPU) -> AppState.processorSelector 갱신
  - Scenario 버튼 -> MainViewModel.applyScenario() 호출 -> 50개 중 필요한 Context 값 일괄 변경
  - 변경 즉시 다른 탭(Tab2 등)에서도 반영(동일 StateFlow 구독)

- 산출물:
  - Docs/05_시나리오 및 상태 전이 로직.md 업데이트
  - app/src/main/java/<package>/ui/screens/tab4/SystemControlScreen.kt
  - app/src/main/java/<package>/domain/scenario/ScenarioPreset.kt
"""

### 완료 기준(Definition of Done)

- Tab4에서 Debug Mode를 켜면 Tab2/Tab3에서도 “디버그 UI/상세 로그”가 활성화될 수 있는 상태
- 시나리오 버튼 1개 이상이 실제로 ToolState를 대량 업데이트하고, UI가 즉시 갱신

---

## 3단계: Neural Grid (Tab 2) - 50개 도구 카드 + 바텀시트 편집

**목표**: 50개 도구를 카드로 보여주고, Debug Mode일 때 도구 타입에 맞는 편집 UI를 제공합니다.

### 참고(Reference)

- `Docs/02_기능 명세서.md` (Tab 2 UI/Bottom Sheet)
- `Docs/03_UI 디자인 시스템 가이드.md` (Glassmorphism/코너/보더/모션)
- `ToolSchema/Context_tool_schema.json`, `ToolSchema/Action_tool_schema.json` (타입/enum)

### 작업 대상(Target)

- `app/src/main/java/<package>/ui/screens/tab2/NeuralGridScreen.kt`
- `app/src/main/java/<package>/ui/screens/tab2/components/ToolCard.kt`
- `app/src/main/java/<package>/ui/screens/tab2/components/ToolEditBottomSheet.kt`
- `app/src/main/java/<package>/ui/screens/tab2/components/ToolCategoryChips.kt`
- `app/src/main/java/<package>/ui/MainViewModel.kt` (개별 도구 업데이트 함수 연결)

### AI 작업 프롬프트(Prompt)

"""
Tab2(Neural Grid)를 구현해주세요.

- 참고:
  - Docs/02_기능 명세서.md 의 2.1, 2.2
  - Docs/03_UI 디자인 시스템 가이드.md (24dp corner, 0.5dp border, alpha 0.8, blur 16dp, duration 500ms)

- 목표:
  - Context/Action 필터링 칩 제공
  - 50개 도구를 카드 그리드로 표시(제목, 현재 값)
  - Debug Mode일 때 카드 클릭 -> BottomSheet -> 타입(Boolean/Number/Enum/String)에 맞는 편집 컨트롤 제공
  - 편집 결과는 MainViewModel의 update API를 통해 ToolState에 반영

- 산출물:
  - app/src/main/java/<package>/ui/screens/tab2/NeuralGridScreen.kt
  - app/src/main/java/<package>/ui/screens/tab2/components/ToolEditBottomSheet.kt
"""

### 완료 기준(Definition of Done)

- Debug Mode OFF: 읽기 전용
- Debug Mode ON: 최소 3종 타입(Boolean/Number/Enum)의 편집이 실제로 상태를 바꾸고 UI에 반영
- Tab4에서 바꾼 시나리오가 Tab2 카드에 즉시 반영

---

## 4단계: Thinking Log (Tab 3) - 추론(thought) + I/O JSON 타임라인

**목표**: “설명 가능한 UI”를 위해 thought와 입력/출력 JSON 로그를 축적/표시합니다.

### 참고(Reference)

- `Docs/02_기능 명세서.md` (Tab 3 요구사항)
- `Docs/04_도구 마스터 레지스트리.md` (tool output/input strict policy)

### 작업 대상(Target)

- `app/src/main/java/<package>/domain/log/LogEntry.kt` (Input/Output/System)
- `app/src/main/java/<package>/domain/log/LogRepository.kt` (in-memory)
- `app/src/main/java/<package>/ui/screens/tab3/ThinkingLogScreen.kt`
- `app/src/main/java/<package>/ui/screens/tab3/components/ThoughtBubble.kt`
- `app/src/main/java/<package>/ui/screens/tab3/components/JsonTimeline.kt`
- `app/src/main/java/<package>/ui/screens/tab3/components/JsonFormatter.kt` (가독성/들여쓰기)

### AI 작업 프롬프트(Prompt)

"""
Tab3(Thinking Log)에 thought 버블과 JSON 타임라인을 구현해주세요.

- 참고:
  - Docs/02_기능 명세서.md 의 3.1, 3.2

- 목표:
  - ViewModel에서 (1) thought 텍스트 스트림, (2) I/O JSON 로그 리스트를 관리
  - 새로운 thought가 오면 타이핑 효과로 표시
  - 로그는 타임스탬프 포함, Input/Output/System을 구분
  - Debug Mode일 때만 System 로그(성능/메모리 등) 표시 영역 노출

- 산출물:
  - app/src/main/java/<package>/ui/screens/tab3/ThinkingLogScreen.kt
  - app/src/main/java/<package>/domain/log/LogEntry.kt
"""

### 완료 기준(Definition of Done)

- 임시(더미) thought/JSON 로그를 ViewModel에서 주입해도 UI가 정상 동작
- Debug Mode ON/OFF에 따라 System 로그 영역 노출이 토글됨

---

## 5단계: Aegis Digital Twin (Tab 1) - Layered View + Action 매핑 애니메이션

**목표**: 상태 기반(Layered View) 오버레이와 Action 트리거 애니메이션을 구현합니다.

### 참고(Reference)

- `Docs/02_기능 명세서.md` (Layered View 구조 / State-Driven)
- `Docs/03_UI 디자인 시스템 가이드.md` (Pulse/500ms)
- `Docs/04_도구 마스터 레지스트리.md` (도구별 트리거 조건/권장 UI 전략)

### 작업 대상(Target)

- `app/src/main/java/<package>/ui/screens/tab1/DigitalTwinScreen.kt`
- `app/src/main/java/<package>/ui/screens/tab1/components/LayeredVehicleView.kt`
- `app/src/main/java/<package>/ui/screens/tab1/components/ViewModeToggle.kt` (Top-view <-> Interior-view)
- `app/src/main/res/drawable/vehicle_top_base.png` (신규 에셋)
- `app/src/main/res/drawable/vehicle_interior_base.png` (신규 에셋)
- `app/src/main/res/drawable/overlay_lane_departure.png` (신규 에셋)
- `app/src/main/res/drawable/overlay_forward_collision.png` (신규 에셋)
- `app/src/main/res/drawable/overlay_steering_vibration.png` (신규 에셋)

### AI 작업 프롬프트(Prompt)

"""
Tab1(Digital Twin)에 Layered View를 구현해주세요.

- 참고:
  - Docs/02_기능 명세서.md 의 1.1~1.2
  - Docs/04_도구 마스터 레지스트리.md 에서 최소 3개 도구(예: get_lane_departure_status, get_forward_collision_risk, trigger_steering_vibration)를 선택해 트리거 조건대로 오버레이/애니메이션을 구현

- 목표:
  - Base Layer(차량 이미지) + Status Layer(Context 오버레이) + Action Layer(Action 애니메이션) + Interaction Layer(뷰 전환)
  - ViewModel 상태 변화로만 애니메이션이 트리거되도록 구성

- 산출물:
  - app/src/main/java/<package>/ui/screens/tab1/DigitalTwinScreen.kt
  - app/src/main/java/<package>/ui/screens/tab1/components/LayeredVehicleView.kt
"""

### 완료 기준(Definition of Done)

- ViewModel에서 해당 tool 값만 바꿔도(시뮬레이션) 오버레이가 즉시 반응
- Top/Interior 뷰 전환이 자연스럽고 상태가 유지됨

---

## 6단계: 디자인 폴리싱 및 성능 최적화 (Polish)

**목표**: 테마 통일, 글래스모피즘 디테일, 리컴포지션 최적화를 통해 완성도를 올립니다.

### 참고(Reference)

- `Docs/03_UI 디자인 시스템 가이드.md`

### 작업 대상(Target)

- `app/src/main/java/<package>/ui/theme/*` (토큰 누락 보완)
- `app/src/main/java/<package>/ui/screens/**` (컴포넌트 통일 / 과도한 recomposition 제거)

### AI 작업 프롬프트(Prompt)

"""
전체 UI를 Pristine Lab 디자인 시스템에 맞게 폴리싱해주세요.

- 참고:
  - Docs/03_UI 디자인 시스템 가이드.md

- 목표:
  - 카드/패널: 24dp corner, 0.5dp border(alpha 0.2), glass alpha 0.8, blur 16dp
  - 애니메이션: 기본 500ms + FastOutSlowInEasing, 액션 트리거는 Pulse
  - 성능: 50개 상태가 동시에 변해도 스크롤/애니메이션이 끊기지 않도록 recomposition 최소화
"""

### 완료 기준(Definition of Done)

- 디자인 토큰이 화면 전체에 일관 적용
- 시나리오 주입(대량 업데이트) 시에도 UI 프레임 드랍이 과도하지 않음
