package com.aegis.das.domain.state

import com.aegis.das.domain.tools.ToolId

data class ToolState(
    val id: ToolId,
    val payload: Map<String, Any?>,
    val updatedAt: Long = System.currentTimeMillis()
)
