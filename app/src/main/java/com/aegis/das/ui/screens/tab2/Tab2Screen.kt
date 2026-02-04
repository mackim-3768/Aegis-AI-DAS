package com.aegis.das.ui.screens.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.ToolState

@Composable
fun Tab2Screen(state: AppState) {
    val activeActions = state.actionStates.values
        .filter { it.payload.isNotEmpty() }
        .sortedBy { it.id.toolName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Neural Grid", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Context tools: ${state.contextStates.size} | Action tools: ${state.actionStates.size}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Last Inference", style = MaterialTheme.typography.titleMedium)
        Text("Severity: ${state.lastInference.severity}", style = MaterialTheme.typography.bodyMedium)
        if (state.lastInference.summary.isNotBlank()) {
            Text(state.lastInference.summary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Active Actions", style = MaterialTheme.typography.titleMedium)
        if (activeActions.isEmpty()) {
            Text("No action payloads yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(activeActions) { toolState ->
                    ActionStateItem(toolState)
                }
            }
        }
    }
}

@Composable
private fun ActionStateItem(toolState: ToolState) {
    val payloadString = toolState.payload.entries
        .sortedBy { it.key }
        .joinToString(", ") { "${it.key}=${it.value}" }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(toolState.id.toolName, style = MaterialTheme.typography.titleSmall)
        Text(payloadString, style = MaterialTheme.typography.bodySmall)
        Text("updatedAt=${toolState.updatedAt}", style = MaterialTheme.typography.labelSmall)
    }
}
