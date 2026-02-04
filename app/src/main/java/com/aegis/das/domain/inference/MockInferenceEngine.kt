package com.aegis.das.domain.inference

import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.tools.ToolId

class MockInferenceEngine : InferenceEngine {
    override fun infer(appState: AppState): InferenceResult {
        val actions = mutableMapOf<ToolId, ActionCall>()
        val eventMessages = mutableListOf<String>()
        var severity = 0

        fun addAction(toolId: ToolId, args: Map<String, Any?>, priority: Int) {
            val existing = actions[toolId]
            if (existing == null || priority > existing.priority) {
                actions[toolId] = ActionCall(toolId, args, priority)
            }
        }

        fun boolValue(toolId: ToolId, key: String, default: Boolean = false): Boolean {
            val payload = appState.contextStates[toolId]?.payload.orEmpty()
            return payload[key] as? Boolean ?: default
        }

        fun stringValue(toolId: ToolId, key: String, default: String = ""): String {
            val payload = appState.contextStates[toolId]?.payload.orEmpty()
            return payload[key] as? String ?: default
        }

        fun numberValue(toolId: ToolId, key: String, default: Double = 0.0): Double {
            val payload = appState.contextStates[toolId]?.payload.orEmpty()
            return when (val value = payload[key]) {
                is Number -> value.toDouble()
                else -> default
            }
        }

        // Rule 1: Driver fatigue
        val drowsy = boolValue(ToolId.GET_DRIVER_DROWSINESS_STATUS, "value")
        val durationLevel = stringValue(ToolId.GET_DRIVING_DURATION_STATUS, "level", "low")
        val gaze = stringValue(ToolId.GET_DRIVER_GAZE_DIRECTION, "direction", "forward")
        val co2Level = stringValue(ToolId.GET_CABIN_CO2_CONCENTRATION, "level", "normal")
        if (drowsy || (durationLevel == "high" && gaze == "down")) {
            val ruleSeverity = if (drowsy && (durationLevel == "high" || co2Level == "high")) 3 else 2
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "Driver fatigue detected"
            addAction(
                ToolId.TRIGGER_DROWSINESS_ALERT_SOUND,
                mapOf("enabled" to true, "level" to "high"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_VOICE_PROMPT,
                mapOf(
                    "message" to "졸음이 감지되었습니다. 가까운 휴게소에서 휴식을 권장합니다.",
                    "level" to if (ruleSeverity >= 3) "danger" else "warning"
                ),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_REST_RECOMMENDATION,
                mapOf("reason" to "fatigue", "level" to if (ruleSeverity >= 3) "high" else "mid"),
                ruleSeverity
            )
            addAction(
                ToolId.RELEASE_REFRESHING_SCENT,
                mapOf("enabled" to true, "duration_ms" to 120000),
                ruleSeverity
            )
            addAction(
                ToolId.UPDATE_AMBIENT_MOOD_LIGHTING,
                mapOf("level" to if (ruleSeverity >= 3) "danger" else "warning", "pulse" to true),
                ruleSeverity
            )
        }

        // Rule 2: Forward collision
        val collisionLevel = stringValue(ToolId.GET_FORWARD_COLLISION_RISK, "level", "low")
        if (collisionLevel == "mid" || collisionLevel == "high") {
            val ruleSeverity = if (collisionLevel == "high") 3 else 2
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "Forward collision risk"
            addAction(
                ToolId.TRIGGER_HUD_WARNING,
                mapOf("message" to "전방 충돌 위험", "level" to "danger"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_CLUSTER_VISUAL_WARNING,
                mapOf("message" to "BRAKE NOW", "level" to "danger"),
                ruleSeverity
            )
            addAction(
                ToolId.PRE_TENSION_SAFETY_BELTS,
                mapOf("enabled" to true, "level" to "high"),
                ruleSeverity
            )
            addAction(
                ToolId.ACTIVATE_HAZARD_WARNING_SIGNALS,
                mapOf("enabled" to true, "duration_ms" to 5000),
                ruleSeverity
            )
            if (ruleSeverity >= 3) {
                addAction(
                    ToolId.EXECUTE_EMERGENCY_STOP_PULL_OVER,
                    mapOf("enabled" to true, "reason" to "forward_collision"),
                    ruleSeverity
                )
            }
        }

        // Rule 3: System intrusion
        val intrusionLevel = stringValue(ToolId.GET_VEHICLE_SYSTEM_INTRUSION_STATUS, "level", "low")
        if (intrusionLevel == "high" || intrusionLevel == "critical") {
            val ruleSeverity = if (intrusionLevel == "critical") 3 else 2
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "System intrusion"
            addAction(
                ToolId.REQUEST_SAFE_MODE,
                mapOf("enabled" to true, "reason" to "system_intrusion"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_NAVIGATION_NOTIFICATION,
                mapOf("message" to "시스템 위협 감지. 안전 모드 전환", "level" to "danger"),
                ruleSeverity
            )
        }

        // Rule 4: Weather & visibility
        val weather = stringValue(ToolId.GET_DRIVING_ENVIRONMENT, "weather", "clear")
        val visibility = stringValue(ToolId.GET_DRIVING_ENVIRONMENT, "visibility_level", "good")
        val roadCondition = stringValue(ToolId.GET_DRIVING_ENVIRONMENT, "road_condition", "dry")
        if (weather in listOf("rain", "fog", "snow") || visibility in listOf("moderate", "poor")) {
            val ruleSeverity = if (visibility == "poor" || (weather == "snow" && roadCondition == "icy")) 2 else 1
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "Low visibility"
            addAction(
                ToolId.TRIGGER_NAVIGATION_NOTIFICATION,
                mapOf("message" to "가시거리 저하. 속도를 줄이세요", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_VOICE_PROMPT,
                mapOf("message" to "가시거리가 낮습니다. 안전 운전 모드로 전환합니다.", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.UPDATE_AMBIENT_MOOD_LIGHTING,
                mapOf("level" to "warning", "pulse" to false),
                ruleSeverity
            )
            addAction(
                ToolId.CONTROL_WINDOW_AND_SUNROOF,
                mapOf("target" to "all", "action" to "close"),
                ruleSeverity
            )
        }

        // Rule 5: Low friction
        val frictionLevel = stringValue(ToolId.GET_ROAD_SURFACE_FRICTION, "level", "high")
        if (frictionLevel == "low" || frictionLevel == "mid") {
            val ruleSeverity = if (frictionLevel == "low") 2 else 1
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "Low friction"
            addAction(
                ToolId.TRIGGER_CLUSTER_VISUAL_WARNING,
                mapOf("message" to "미끄러운 노면", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_HUD_WARNING,
                mapOf("message" to "LOW TRACTION", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.ACTIVATE_HAZARD_WARNING_SIGNALS,
                mapOf("enabled" to true, "duration_ms" to 4000),
                ruleSeverity
            )
            addAction(
                ToolId.ADJUST_SEAT_BOLSTER_FIRMNESS,
                mapOf("level" to "mid"),
                ruleSeverity
            )
        }

        // Rule 6: Emergency vehicle proximity
        val emergencyVehicle = boolValue(ToolId.GET_V2X_EMERGENCY_VEHICLE_PROXIMITY, "value")
        if (emergencyVehicle) {
            val distance = numberValue(ToolId.GET_V2X_EMERGENCY_VEHICLE_PROXIMITY, "distance", 0.0)
            val direction = stringValue(ToolId.GET_V2X_EMERGENCY_VEHICLE_PROXIMITY, "direction", "unknown")
            val ruleSeverity = if (distance <= 200.0 || direction != "unknown") 2 else 1
            severity = maxOf(severity, ruleSeverity)
            eventMessages += "Emergency vehicle nearby"
            addAction(
                ToolId.TRIGGER_NAVIGATION_NOTIFICATION,
                mapOf("message" to "긴급 차량 접근. 양보하세요", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.TRIGGER_VOICE_PROMPT,
                mapOf("message" to "긴급 차량이 접근 중입니다.", "level" to "warning"),
                ruleSeverity
            )
            addAction(
                ToolId.UPDATE_AMBIENT_MOOD_LIGHTING,
                mapOf("level" to "warning", "pulse" to true),
                ruleSeverity
            )
        }

        if (eventMessages.isNotEmpty()) {
            val logLevel = when {
                severity >= 3 -> "danger"
                severity >= 2 -> "warning"
                else -> "info"
            }
            addAction(
                ToolId.LOG_SAFETY_EVENT,
                mapOf(
                    "event_type" to "mock_inference",
                    "message" to eventMessages.joinToString(" | "),
                    "level" to logLevel
                ),
                severity
            )
        }

        if (severity > 0) {
            addAction(
                ToolId.ESCALATE_WARNING_LEVEL,
                mapOf("level" to escalationLevel(severity)),
                severity
            )
        }

        val summary = if (eventMessages.isEmpty()) {
            "No critical events detected"
        } else {
            "Triggered: ${eventMessages.joinToString()}"
        }

        return InferenceResult(
            thought = summary,
            summary = summary,
            severity = severity,
            actionCalls = actions.values.sortedByDescending { it.priority },
            rawInput = mapOf(
                "context" to appState.contextStates.mapValues { it.value.payload },
                "processor" to appState.processor.name,
                "debug" to appState.debugMode
            ),
            rawOutput = mapOf(
                "severity" to severity,
                "actions" to actions.values.map { it.toolId.toolName }
            )
        )
    }

    private fun escalationLevel(severity: Int): String = when (severity) {
        1 -> "mid"
        2 -> "high"
        3 -> "critical"
        else -> "low"
    }
}
