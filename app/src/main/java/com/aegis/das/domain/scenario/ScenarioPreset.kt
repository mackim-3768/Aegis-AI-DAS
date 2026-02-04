package com.aegis.das.domain.scenario

import com.aegis.das.domain.tools.ToolId

data class ScenarioPreset(
    val id: ScenarioId,
    val contextOverrides: Map<ToolId, Map<String, Any?>>
) {
    val label: String = id.label
}

object ScenarioPresets {
    val all: List<ScenarioPreset> = listOf(
        ScenarioPreset(
            ScenarioId.DRIVER_FATIGUE,
            mapOf(
                ToolId.GET_DRIVER_DROWSINESS_STATUS to mapOf("value" to true, "confidence" to 0.88),
                ToolId.GET_DRIVING_DURATION_STATUS to mapOf("value" to 5400.0, "level" to "high"),
                ToolId.GET_DRIVER_GAZE_DIRECTION to mapOf("direction" to "down", "confidence" to 0.7),
                ToolId.GET_CABIN_CO2_CONCENTRATION to mapOf("value" to 1500.0, "level" to "high")
            )
        ),
        ScenarioPreset(
            ScenarioId.FORWARD_COLLISION,
            mapOf(
                ToolId.GET_FORWARD_COLLISION_RISK to mapOf("score" to 0.92, "level" to "high", "confidence" to 0.9),
                ToolId.GET_VEHICLE_SPEED to mapOf("value" to 85.0)
            )
        ),
        ScenarioPreset(
            ScenarioId.SYSTEM_INTRUSION,
            mapOf(
                ToolId.GET_VEHICLE_SYSTEM_INTRUSION_STATUS to mapOf("value" to true, "level" to "critical", "confidence" to 0.95)
            )
        ),
        ScenarioPreset(
            ScenarioId.LOW_VISIBILITY,
            mapOf(
                ToolId.GET_DRIVING_ENVIRONMENT to mapOf(
                    "weather" to "fog",
                    "road_condition" to "wet",
                    "visibility_level" to "poor"
                )
            )
        ),
        ScenarioPreset(
            ScenarioId.LOW_FRICTION,
            mapOf(
                ToolId.GET_ROAD_SURFACE_FRICTION to mapOf("value" to 0.2, "level" to "low"),
                ToolId.GET_DRIVING_ENVIRONMENT to mapOf(
                    "weather" to "snow",
                    "road_condition" to "icy",
                    "visibility_level" to "moderate"
                )
            )
        ),
        ScenarioPreset(
            ScenarioId.EMERGENCY_VEHICLE,
            mapOf(
                ToolId.GET_V2X_EMERGENCY_VEHICLE_PROXIMITY to mapOf(
                    "value" to true,
                    "distance" to 120.0,
                    "direction" to "rear",
                    "confidence" to 0.9
                )
            )
        )
    )
}
