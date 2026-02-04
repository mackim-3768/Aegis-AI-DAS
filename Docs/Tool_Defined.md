## 통합 주행 보조 시스템 도구 명세서 (Total: 50)

이 시스템은 차량의 상태를 인지하는 **Context Tools**와 인지된 상황에 대응하는 **Action Tools**로 구성됩니다.

### 1. Context Tools: 상황 인지 및 데이터 수집 (25개)

차량 내부 센서, AI 모델, 그리고 외부 데이터를 통해 현재의 주행 환경을 실시간으로 파악합니다.

| 분류 | 함수명(Function Name) | 주요 기능 요약 |
| --- | --- | --- |
| **운전자 모니터링** | `get_driver_drowsiness_status` | 졸음 운전 여부 및 탐지 신뢰도 확인 |
|  | `get_steering_grip_status` | 운전자가 핸들을 잡고 있는지(Hands-on) 감지 |
|  | `get_driver_vital_signs` | 운전자 심박수, 체온 등 생체 데이터 측정 |
|  | `get_driver_gaze_direction` | 시선 추적을 통한 전방 주시 태만 여부 판단 |
|  | `get_driver_stress_index` | HRV 기반 운전자 스트레스 및 분노 수치 계산 |
| **주행 제어 및 이력** | `get_vehicle_speed` | 현재 차량의 실시간 주행 속도(kph) 조회 |
|  | `get_lka_status` | 차선 유지 보조(LKA) 기능 활성화 여부 확인 |
|  | `get_driving_duration_status` | 누적 주행 시간 및 그에 따른 피로도 평가 |
|  | `get_recent_warning_history` | 최근 발생한 경고 유형 및 경과 시간 조회 |
|  | `get_sensor_health_status` | 카메라 및 AI 모델의 작동 정상 여부 체크 |
| **외부 안전 및 노면** | `get_lane_departure_status` | 차선 이탈 여부 및 차선 인식 신뢰도 확인 |
|  | `get_forward_collision_risk` | 전방 충돌 위험도 점수 및 수준(low~high) 평가 |
|  | `get_driving_environment` | 날씨(비, 눈 등), 노면 상태, 가시거리 확인 |
|  | `get_road_surface_friction` | 타이어 슬립을 통한 도로 마찰력/미끄러움 측정 |
|  | `get_blind_spot_collision_risk` | 사각지대 차량 유무 및 차선 변경 위험도 계산 |
|  | `get_external_environmental_hazards` | 도로 위 낙하물, 공사 구간 등 잠재적 위협 식별 |
| **연결성 및 동승자** | `get_v2x_traffic_info` | V2X 통신 기반 신호등 잔여 시간 및 사고 정보 |
|  | `get_v2x_emergency_vehicle_proximity` | 인근 긴급 차량(구급차, 소방차) 접근 확인 |
|  | `get_rear_occupant_status` | 뒷좌석 승객/반려동물 잔류 여부 감지 |
|  | `get_passenger_seat_occupancy` | 각 좌석 탑승객 무게 및 착석 상태 모니터링 |
| **차량 및 보안** | `get_cabin_air_quality` | 실내 미세먼지 및 종합 공기질 수치 측정 |
|  | `get_cabin_co2_concentration` | 졸음을 유발하는 실내  농도 모니터링 |
|  | `get_ev_battery_thermal_status` | 전기차 배터리 온도 및 열관리 시스템 상태 확인 |
|  | `get_trailer_sway_status` | 견인 트레일러의 비정상 흔들림 현상 감지 |
|  | `get_vehicle_system_intrusion_status` | 차량 네트워크 해킹 및 사이버 보안 위협 감시 |

---

### 2. Action Tools: 능동적 제어 및 대응 (25개)

인지된 위험 수준에 따라 운전자에게 경고를 주거나 차량의 물리적 장치를 직접 제어합니다.

| 분류 | 함수명(Function Name) | 주요 기능 요약 |
| --- | --- | --- |
| **시·청·촉각 경고** | `trigger_steering_vibration` | 핸들 진동(low~high)을 통한 주의 환기 |
|  | `trigger_drowsiness_alert_sound` | 졸음 방지 전용 알람음 재생 |
|  | `trigger_cluster_visual_warning` | 계기판(Cluster) 내 시각적 경고 메시지 표시 |
|  | `trigger_hud_warning` | 헤드업 디스플레이(HUD)에 경고 그래픽 투사 |
|  | `trigger_voice_prompt` | 운전자에게 음성 안내 메시지 전달 |
|  | `update_ambient_mood_lighting` | 위험 시 실내 조명 색상 변경(예: 빨간색 점멸) |
|  | `release_refreshing_scent` | 집중력 향상을 위한 상쾌한 향기 분사 |
| **시스템 격상 및 기록** | `escalate_warning_level` | 전체 시스템의 경고 단계 강제 상향 제어 |
|  | `request_safe_mode` | 차량을 즉시 안전 모드로 전환하도록 요청 |
|  | `log_safety_event` | 발생한 안전 이벤트를 블랙박스/로그에 기록 |
|  | `trigger_navigation_notification` | 내비게이션 화면에 위험 안내 팝업 표시 |
|  | `trigger_rest_recommendation` | 사유 명시를 통한 가까운 졸음 쉼터 안내 |
| **능동적 물리 제어** | `pre_tension_safety_belts` | 충돌 직전 안전벨트를 당겨 탑승객 밀착 고정 |
|  | `activate_hazard_warning_signals` | 급제동 시 비상등을 고속으로 자동 점멸 |
|  | `execute_emergency_stop_pull_over` | 비상 상황 시 차량을 갓길로 유도 후 자동 정차 |
|  | `adjust_seat_bolster_firmness` | 역동적 주행 시 시트 지지력을 조절하여 몸 고정 |
|  | `control_window_and_sunroof` | 환기 또는 우천 시 창문/선루프 자동 개폐 제어 |
| **승객 및 보행자 보호** | `trigger_emergency_call` | 사고 발생 시 e-Call 긴급 구호 자동 연결 |
|  | `trigger_ground_projection_warning` | 노면에 보행자 주의 경고 그래픽 투사 |
|  | `emit_external_pedestrian_alert` | 보행자를 위한 외부 가상 주행음 출력 |
|  | `deploy_active_pedestrian_protection` | 보행자 충돌 시 보닛을 들어 올려 충격 완화 |
| **환경 및 편의** | `activate_cabin_purification` | 오염 감지 시 실내 공기 청정 모드 즉시 가동 |
|  | `switch_cabin_air_circulation` | 미세먼지 차단을 위한 내/외기 순환 모드 전환 |
|  | `activate_wellness_massage` | 운전자 피로 시 시트 마사지 기능 활성화 |
|  | `set_valet_mode_limitations` | 대리 운전 시 차량 성능 및 개인 데이터 제한 |
