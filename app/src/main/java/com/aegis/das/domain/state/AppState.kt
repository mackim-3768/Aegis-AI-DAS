package com.aegis.das.domain.state

import com.aegis.das.domain.tools.ToolId

data class AppState(
    val debugMode: Boolean = false,
    val processor: ProcessorType = ProcessorType.CPU,
    val contextStates: Map<ToolId, ToolState> = ToolDefaults.defaultContextStates(),
    val actionStates: Map<ToolId, ToolState> = ToolDefaults.defaultActionStates(),
    val lastInference: InferenceSnapshot = InferenceSnapshot(),
    val logEntries: List<LogEntry> = emptyList()
)
