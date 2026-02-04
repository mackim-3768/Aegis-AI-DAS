package com.aegis.das.domain.state

data class InferenceSnapshot(
    val thought: String = "",
    val summary: String = "",
    val severity: Int = 0,
    val actionToolNames: List<String> = emptyList(),
    val timestamp: Long = 0L
)
