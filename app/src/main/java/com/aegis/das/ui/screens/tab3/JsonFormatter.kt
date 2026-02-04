package com.aegis.das.ui.screens.tab3

import com.aegis.das.domain.tools.ToolId
import org.json.JSONArray
import org.json.JSONObject

internal object JsonFormatter {
    fun format(value: Any?): String {
        val jsonValue = toJsonValue(value)
        return when (jsonValue) {
            is JSONObject -> jsonValue.toString(2)
            is JSONArray -> jsonValue.toString(2)
            null -> "null"
            else -> jsonValue.toString()
        }
    }

    private fun toJsonValue(value: Any?): Any? {
        return when (value) {
            null -> null
            is JSONObject -> value
            is JSONArray -> value
            is Map<*, *> -> {
                val converted = value.entries.associate { (k, v) ->
                    val key = when (k) {
                        null -> "null"
                        is ToolId -> k.toolName
                        else -> k.toString()
                    }
                    key to toJsonValue(v)
                }
                JSONObject(converted)
            }

            is Iterable<*> -> JSONArray(value.map { toJsonValue(it) })
            is Array<*> -> JSONArray(value.map { toJsonValue(it) })
            is Boolean, is Int, is Long, is Float, is Double, is String -> value
            else -> value.toString()
        }
    }
}
