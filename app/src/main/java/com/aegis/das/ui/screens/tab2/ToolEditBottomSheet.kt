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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolId
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ToolEditBottomSheet(
    toolState: ToolState,
    onDismissRequest: () -> Unit,
    onFieldUpdated: (String, Any?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(toolState.id.toolName, style = MaterialTheme.typography.titleLarge)

            val sortedKeys = toolState.payload.keys.sorted()
            sortedKeys.forEach { key ->
                val value = toolState.payload[key]
                FieldEditor(
                    toolId = toolState.id,
                    fieldKey = key,
                    value = value,
                    onValueChanged = { onFieldUpdated(key, it) }
                )
            }

            Button(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun FieldEditor(
    toolId: ToolId,
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(fieldKey, style = MaterialTheme.typography.bodyMedium)
                Text(value?.toString() ?: "null", style = MaterialTheme.typography.bodySmall)
            }
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
