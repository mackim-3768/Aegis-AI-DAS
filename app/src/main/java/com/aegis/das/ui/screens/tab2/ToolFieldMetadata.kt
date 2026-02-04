package com.aegis.das.ui.screens.tab2

internal object ToolFieldMetadata {
    fun sliderRangeFor(key: String): ClosedFloatingPointRange<Float>? = when (key) {
        "confidence" -> 0f..1f
        "score" -> 0f..1f
        "heart_rate" -> 40f..180f
        "body_temperature" -> 34f..40f
        "value" -> 0f..200f
        "distance" -> 0f..500f
        "temperature" -> -10f..90f
        "time_to_change" -> 0f..120f
        "duration_ms" -> 0f..3000f
        else -> null
    }

    fun sliderStepsFor(key: String): Int = when (key) {
        "duration_ms" -> 30
        "value" -> 40
        else -> 20
    }
}
