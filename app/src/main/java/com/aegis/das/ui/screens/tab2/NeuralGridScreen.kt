package com.aegis.das.ui.screens.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolId
import com.aegis.das.domain.tools.ToolKind

@Composable
internal fun NeuralGridScreen(
    state: AppState,
    onToolUpdated: (ToolId, Map<String, Any?>) -> Unit
) {
    var selectedKind by remember { mutableStateOf(ToolKind.CONTEXT) }
    var editingToolId by remember { mutableStateOf<ToolId?>(null) }

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
                    onClick = { editingToolId = toolState.id }
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
}
