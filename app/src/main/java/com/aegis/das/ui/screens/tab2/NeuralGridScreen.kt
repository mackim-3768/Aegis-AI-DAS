package com.aegis.das.ui.screens.tab2

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolId
import com.aegis.das.domain.tools.ToolKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Composable
internal fun NeuralGridScreen(
    state: AppState,
    onToolUpdated: (ToolId, Map<String, Any?>) -> Unit
) {
    var selectedKind by remember { mutableStateOf(ToolKind.CONTEXT) }
    var editingToolId by remember { mutableStateOf<ToolId?>(null) }
    var infoToolId by remember { mutableStateOf<ToolId?>(null) }

    val context = LocalContext.current
    var toolDescriptions by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(context) {
        toolDescriptions = loadToolDescriptions(context)
    }

    LaunchedEffect(state.debugMode) {
        if (state.debugMode) {
            infoToolId = null
        }
    }

    val toolStates: List<ToolState> = remember(state.contextStates, state.actionStates, selectedKind) {
        when (selectedKind) {
            ToolKind.CONTEXT -> state.contextStates.values.sortedBy { it.id.toolName }
            ToolKind.ACTION -> state.actionStates.values.sortedBy { it.id.toolName }
        }
    }

    val editingState: ToolState? = editingToolId?.let { id ->
        when (id.kind) {
            ToolKind.CONTEXT -> state.contextStates[id]
            ToolKind.ACTION -> state.actionStates[id]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        ToolKindFilterRow(
            selected = selectedKind,
            onSelected = { selectedKind = it }
        )

        Text(
            text = "Tools: ${toolStates.size} | Debug Edit: ${state.debugMode}",
            style = MaterialTheme.typography.labelMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(toolStates, key = { it.id }) { toolState ->
                ToolCard(
                    toolState = toolState,
                    enabled = state.debugMode,
                    onClick = { editingToolId = toolState.id },
                    onDisabledClick = { infoToolId = toolState.id }
                )
            }
        }
    }

    if (state.debugMode && editingState != null) {
        ToolEditBottomSheet(
            toolState = editingState,
            onDismissRequest = { editingToolId = null },
            onApplyChanges = { changes ->
                if (changes.isNotEmpty()) {
                    onToolUpdated(editingState.id, changes)
                }
            }
        )
    }

    if (!state.debugMode && infoToolId != null) {
        val toolName = infoToolId?.toolName.orEmpty()
        val description = when {
            toolName.isBlank() -> "설명이 없습니다."
            toolDescriptions.containsKey(toolName) -> toolDescriptions.getValue(toolName)
            toolDescriptions.isEmpty() -> "설명을 불러오는 중입니다."
            else -> "설명이 없습니다."
        }
        ToolDescriptionBottomSheet(
            toolName = toolName.ifBlank { "Tool" },
            description = description,
            onDismissRequest = { infoToolId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolDescriptionBottomSheet(
    toolName: String,
    description: String,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = toolName, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private suspend fun loadToolDescriptions(context: Context): Map<String, String> = withContext(Dispatchers.IO) {
    val output = LinkedHashMap<String, String>()
    val files = listOf("Context_tool_schema.json", "Action_tool_schema.json")
    files.forEach { fileName ->
        try {
            context.assets.open(fileName).use { input ->
                val json = JSONArray(input.bufferedReader().readText())
                for (index in 0 until json.length()) {
                    val fn = json.optJSONObject(index)?.optJSONObject("function") ?: continue
                    val name = fn.optString("name")
                    val desc = fn.optString("description")
                    if (name.isNotBlank() && desc.isNotBlank()) {
                        output[name] = desc
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore missing or malformed schema files.
        }
    }
    output
}
