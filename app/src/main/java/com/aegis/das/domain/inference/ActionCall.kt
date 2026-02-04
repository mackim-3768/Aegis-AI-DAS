package com.aegis.das.domain.inference

import com.aegis.das.domain.tools.ToolId

data class ActionCall(
    val toolId: ToolId,
    val args: Map<String, Any?>,
    val priority: Int = 0
)
