package com.aegis.das.domain.inference

data class InferenceResult(
    val thought: String,
    val summary: String,
    val severity: Int,
    val actionCalls: List<ActionCall>,
    val rawInput: Map<String, Any?>,
    val rawOutput: Map<String, Any?>
)
