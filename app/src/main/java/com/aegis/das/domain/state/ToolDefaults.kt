package com.aegis.das.domain.state

import com.aegis.das.domain.tools.ToolId
import com.aegis.das.domain.tools.ToolKind

object ToolDefaults {
    private val contextDefaults: Map<ToolId, Map<String, Any?>> = mapOf(
        ToolId.GET_DRIVER_DROWSINESS_STATUS to mapOf("value" to false, "confidence" to 0.0),
        ToolId.GET_STEERING_GRIP_STATUS to mapOf("value" to true, "confidence" to 1.0),
        ToolId.GET_DRIVER_VITAL_SIGNS to mapOf("heart_rate" to 72, "body_temperature" to 36.5, "confidence" to 1.0),
        ToolId.GET_DRIVER_GAZE_DIRECTION to mapOf("direction" to "forward", "confidence" to 1.0),
        ToolId.GET_DRIVER_STRESS_INDEX to mapOf("value" to 0.1, "level" to "low"),
        ToolId.GET_VEHICLE_SPEED to mapOf("value" to 0.0),
        ToolId.GET_LKA_STATUS to mapOf("value" to true),
        ToolId.GET_DRIVING_DURATION_STATUS to mapOf("value" to 0.0, "level" to "low"),
        ToolId.GET_RECENT_WARNING_HISTORY to mapOf("warnings" to emptyList<Map<String, Any?>>()),
        ToolId.GET_SENSOR_HEALTH_STATUS to mapOf("overall_ok" to true, "camera_ok" to true, "model_ok" to true),
        ToolId.GET_LANE_DEPARTURE_STATUS to mapOf("value" to false, "confidence" to 1.0),
        ToolId.GET_FORWARD_COLLISION_RISK to mapOf("score" to 0.0, "level" to "low", "confidence" to 1.0),
        ToolId.GET_DRIVING_ENVIRONMENT to mapOf("weather" to "clear", "road_condition" to "dry", "visibility_level" to "good"),
        ToolId.GET_ROAD_SURFACE_FRICTION to mapOf("value" to 0.9, "level" to "high"),
        ToolId.GET_BLIND_SPOT_COLLISION_RISK to mapOf("value" to false, "level" to "low", "confidence" to 1.0),
        ToolId.GET_EXTERNAL_ENVIRONMENTAL_HAZARDS to mapOf("hazards" to emptyList<Map<String, Any?>>(), "confidence" to 1.0),
        ToolId.GET_V2X_TRAFFIC_INFO to mapOf("signal_state" to "unknown", "time_to_change" to 0, "incident_level" to "none"),
        ToolId.GET_V2X_EMERGENCY_VEHICLE_PROXIMITY to mapOf(
            "value" to false,
            "distance" to 0.0,
            "direction" to "unknown",
            "confidence" to 1.0
        ),
        ToolId.GET_REAR_OCCUPANT_STATUS to mapOf("value" to false, "confidence" to 1.0),
        ToolId.GET_PASSENGER_SEAT_OCCUPANCY to mapOf(
            "seats" to listOf(
                mapOf("seat" to "driver", "occupied" to true, "weight" to 70.0),
                mapOf("seat" to "front_passenger", "occupied" to false, "weight" to 0.0),
                mapOf("seat" to "rear_left", "occupied" to false, "weight" to 0.0),
                mapOf("seat" to "rear_center", "occupied" to false, "weight" to 0.0),
                mapOf("seat" to "rear_right", "occupied" to false, "weight" to 0.0)
            ),
            "confidence" to 1.0
        ),
        ToolId.GET_CABIN_AIR_QUALITY to mapOf("value" to 0.0, "level" to "good"),
        ToolId.GET_CABIN_CO2_CONCENTRATION to mapOf("value" to 400.0, "level" to "normal"),
        ToolId.GET_EV_BATTERY_THERMAL_STATUS to mapOf(
            "temperature" to 25.0,
            "level" to "normal",
            "cooling_active" to false
        ),
        ToolId.GET_TRAILER_SWAY_STATUS to mapOf("value" to false, "level" to "low", "confidence" to 1.0),
        ToolId.GET_VEHICLE_SYSTEM_INTRUSION_STATUS to mapOf("value" to false, "level" to "low", "confidence" to 1.0)
    )

    private val actionDefaults: Map<ToolId, Map<String, Any?>> = mapOf(
        ToolId.TRIGGER_STEERING_VIBRATION to mapOf("level" to "low", "duration_ms" to 0),
        ToolId.TRIGGER_DROWSINESS_ALERT_SOUND to mapOf("enabled" to false, "level" to "low"),
        ToolId.TRIGGER_CLUSTER_VISUAL_WARNING to mapOf("message" to "", "level" to "info"),
        ToolId.TRIGGER_HUD_WARNING to mapOf("message" to "", "level" to "info"),
        ToolId.TRIGGER_VOICE_PROMPT to mapOf("message" to "", "level" to "normal"),
        ToolId.UPDATE_AMBIENT_MOOD_LIGHTING to mapOf("level" to "safe", "pulse" to false),
        ToolId.RELEASE_REFRESHING_SCENT to mapOf("enabled" to false, "duration_ms" to 0),
        ToolId.ESCALATE_WARNING_LEVEL to mapOf("level" to "low"),
        ToolId.REQUEST_SAFE_MODE to mapOf("enabled" to false, "reason" to ""),
        ToolId.LOG_SAFETY_EVENT to mapOf("event_type" to "", "message" to "", "level" to "info"),
        ToolId.TRIGGER_NAVIGATION_NOTIFICATION to mapOf("message" to "", "level" to "info"),
        ToolId.TRIGGER_REST_RECOMMENDATION to mapOf("reason" to "", "level" to "low"),
        ToolId.PRE_TENSION_SAFETY_BELTS to mapOf("enabled" to false, "level" to "low"),
        ToolId.ACTIVATE_HAZARD_WARNING_SIGNALS to mapOf("enabled" to false, "duration_ms" to 0),
        ToolId.EXECUTE_EMERGENCY_STOP_PULL_OVER to mapOf("enabled" to false, "reason" to ""),
        ToolId.ADJUST_SEAT_BOLSTER_FIRMNESS to mapOf("level" to "low"),
        ToolId.CONTROL_WINDOW_AND_SUNROOF to mapOf("target" to "window", "action" to "close"),
        ToolId.TRIGGER_EMERGENCY_CALL to mapOf("enabled" to false, "level" to "high"),
        ToolId.TRIGGER_GROUND_PROJECTION_WARNING to mapOf("message" to "", "level" to "warning"),
        ToolId.EMIT_EXTERNAL_PEDESTRIAN_ALERT to mapOf("enabled" to false, "level" to "low"),
        ToolId.DEPLOY_ACTIVE_PEDESTRIAN_PROTECTION to mapOf("enabled" to false),
        ToolId.ACTIVATE_CABIN_PURIFICATION to mapOf("enabled" to false, "level" to "low"),
        ToolId.SWITCH_CABIN_AIR_CIRCULATION to mapOf("mode" to "fresh_air"),
        ToolId.ACTIVATE_WELLNESS_MASSAGE to mapOf("enabled" to false, "duration_ms" to 0),
        ToolId.SET_VALET_MODE_LIMITATIONS to mapOf("enabled" to false, "level" to "low")
    )

    fun allowedKeys(toolId: ToolId): Set<String> =
        contextDefaults[toolId]?.keys
            ?: actionDefaults[toolId]?.keys
            ?: emptySet()

    fun defaultPayload(toolId: ToolId): Map<String, Any?> =
        (contextDefaults[toolId] ?: actionDefaults[toolId]).orEmpty()

    fun defaultToolState(toolId: ToolId): ToolState = ToolState(
        id = toolId,
        payload = defaultPayload(toolId)
    )

    fun defaultContextStates(): Map<ToolId, ToolState> =
        ToolId.contextTools.associateWith { defaultToolState(it) }

    fun defaultActionStates(): Map<ToolId, ToolState> =
        ToolId.actionTools.associateWith { defaultToolState(it) }

    fun isValidKind(toolId: ToolId, kind: ToolKind): Boolean = toolId.kind == kind
}
