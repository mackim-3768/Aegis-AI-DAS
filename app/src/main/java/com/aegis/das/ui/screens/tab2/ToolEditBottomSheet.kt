package com.aegis.das.ui.screens.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolId
import com.aegis.das.ui.screens.tab3.JsonFormatter
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ToolEditBottomSheet(
    toolState: ToolState,
    onDismissRequest: () -> Unit,
    onApplyChanges: (Map<String, Any?>) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        var draft by remember(toolState.id, toolState.updatedAt) {
            mutableStateOf<Map<String, Any?>>(toolState.payload)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(toolState.id.toolName, style = MaterialTheme.typography.titleLarge)

            val sortedKeys = toolState.payload.keys.sorted()
            sortedKeys.forEach { key ->
                val value = draft[key]
                FieldEditor(
                    toolId = toolState.id,
                    sheetKey = toolState.updatedAt,
                    fieldKey = key,
                    value = value,
                    onValueChanged = { nextValue ->
                        draft = draft.toMutableMap().apply { put(key, nextValue) }
                    }
                )
            }

            val hasChanges = draft != toolState.payload
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDismissRequest) {
                    Text("Close")
                }

                Button(
                    onClick = {
                        val changes = draft.filter { (k, v) -> toolState.payload[k] != v }
                        if (changes.isNotEmpty()) {
                            onApplyChanges(changes)
                        }
                        onDismissRequest()
                    },
                    enabled = hasChanges
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

private fun JSONObject.toMap(): Map<String, Any?> {
    val iterator = keys()
    val output = linkedMapOf<String, Any?>()
    while (iterator.hasNext()) {
        val key = iterator.next()
        output[key] = normalizeJsonValue(get(key))
    }
    return output
}

private fun JSONArray.toList(): List<Any?> {
    val output = ArrayList<Any?>(length())
    for (i in 0 until length()) {
        output.add(normalizeJsonValue(get(i)))
    }
    return output
}

private fun normalizeJsonValue(value: Any?): Any? {
    return when (value) {
        null -> null
        JSONObject.NULL -> null
        is JSONObject -> value.toMap()
        is JSONArray -> value.toList()
        else -> value
    }
}

@Composable
private fun FieldEditor(
    toolId: ToolId,
    sheetKey: Long,
    fieldKey: String,
    value: Any?,
    onValueChanged: (Any?) -> Unit
) {
    when (value) {
        is Boolean -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fieldKey, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = value, onCheckedChange = { onValueChanged(it) })
            }
        }

        is Int -> NumberEditor(
            fieldKey = fieldKey,
            value = value.toFloat(),
            isInt = true,
            onValueChanged = { onValueChanged(it.roundToInt()) }
        )

        is Float -> NumberEditor(
            fieldKey = fieldKey,
            value = value,
            isInt = false,
            onValueChanged = { onValueChanged(it) }
        )

        is Double -> NumberEditor(
            fieldKey = fieldKey,
            value = value.toFloat(),
            isInt = false,
            onValueChanged = { onValueChanged(it.toDouble()) }
        )

        is String -> StringEditor(
            toolId = toolId,
            fieldKey = fieldKey,
            value = value,
            onValueChanged = { onValueChanged(it) }
        )

        else -> {
            JsonValueEditor(
                toolId = toolId,
                sheetKey = sheetKey,
                fieldKey = fieldKey,
                value = value,
                onValueChanged = onValueChanged
            )
        }
    }
}

@Composable
private fun JsonValueEditor(
    toolId: ToolId,
    sheetKey: Long,
    fieldKey: String,
    value: Any?,
    onValueChanged: (Any?) -> Unit
) {
    var text by remember(toolId, sheetKey, fieldKey) {
        mutableStateOf(JsonFormatter.format(value))
    }
    var error by remember(toolId, sheetKey, fieldKey) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(fieldKey, style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = text,
            onValueChange = { next ->
                text = next
                val trimmed = next.trim()
                if (trimmed.isEmpty()) {
                    error = null
                    onValueChanged(null)
                    return@OutlinedTextField
                }

                try {
                    val parsed = JSONTokener(trimmed).nextValue()
                    val normalized: Any? = when (parsed) {
                        is JSONObject -> parsed.toMap()
                        is JSONArray -> parsed.toList()
                        JSONObject.NULL -> null
                        else -> parsed
                    }
                    error = null
                    onValueChanged(normalized)
                } catch (t: Throwable) {
                    error = t.message ?: "Invalid JSON"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        )

        if (error != null) {
            Text(
                text = error.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun NumberEditor(
    fieldKey: String,
    value: Float,
    isInt: Boolean,
    onValueChanged: (Float) -> Unit
) {
    val range = ToolFieldMetadata.sliderRangeFor(fieldKey)

    if (range != null) {
        var sliderValue by remember { mutableStateOf(value.coerceIn(range.start, range.endInclusive)) }

        LaunchedEffect(value) {
            sliderValue = value.coerceIn(range.start, range.endInclusive)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val displayValue = if (isInt) sliderValue.roundToInt().toString() else String.format("%.2f", sliderValue)
            Text("$fieldKey: $displayValue", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onValueChanged(it)
                },
                valueRange = range,
                steps = ToolFieldMetadata.sliderStepsFor(fieldKey)
            )
        }
    } else {
        var text by remember { mutableStateOf(value.toString()) }

        LaunchedEffect(value) {
            text = value.toString()
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(fieldKey, style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    val parsed = it.toFloatOrNull()
                    if (parsed != null) onValueChanged(parsed)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun StringEditor(
    toolId: ToolId,
    fieldKey: String,
    value: String,
    onValueChanged: (String) -> Unit
) {
    val options = remember(toolId, fieldKey) { ToolFieldOptions.options(toolId = toolId, key = fieldKey) }
    if (options != null) {
        DropdownEditor(
            fieldKey = fieldKey,
            value = value,
            options = options,
            onValueChanged = onValueChanged
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(fieldKey) },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownEditor(
    fieldKey: String,
    value: String,
    options: List<String>,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text(fieldKey) },
            readOnly = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
