package com.aegis.das.domain.state

data class LogEntry(
    val id: Long,
    val type: LogEntryType,
    val timestamp: Long,
    val message: String? = null,
    val payload: Any? = null
)
