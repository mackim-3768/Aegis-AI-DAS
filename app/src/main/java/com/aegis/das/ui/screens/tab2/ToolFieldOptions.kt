package com.aegis.das.ui.screens.tab2

import com.aegis.das.domain.tools.ToolId

internal object ToolFieldOptions {
    fun options(toolId: ToolId, key: String): List<String>? = when (key) {
        "level" -> listOf(
            "low",
            "mid",
            "high",
            "critical",
            "info",
            "warning",
            "danger",
            "normal",
            "safe",
            "good",
            "moderate",
            "poor"
        )
        "direction" -> listOf("forward", "left", "right", "down", "up", "unknown")
        "weather" -> listOf("clear", "rain", "snow", "fog", "storm")
        "road_condition" -> listOf("dry", "wet", "icy", "snowy", "unknown")
        "visibility_level" -> listOf("good", "moderate", "low", "poor")
        "signal_state" -> listOf("green", "yellow", "red", "unknown")
        "incident_level" -> listOf("none", "low", "medium", "high")
        "mode" -> listOf("fresh_air", "recirculation")
        "target" -> listOf("window", "sunroof")
        "action" -> listOf("open", "close", "vent")
        else -> null
    }
}
