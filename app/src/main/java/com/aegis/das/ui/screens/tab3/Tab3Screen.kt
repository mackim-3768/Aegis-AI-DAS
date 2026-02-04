package com.aegis.das.ui.screens.tab3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState

@Composable
fun Tab3Screen(state: AppState) {
    val snapshot = state.lastInference
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Thinking Log", style = MaterialTheme.typography.headlineSmall)

        Text("Debug: ${state.debugMode}", style = MaterialTheme.typography.labelMedium)
        Text("Processor: ${state.processor}", style = MaterialTheme.typography.labelMedium)

        Text("Last Inference", style = MaterialTheme.typography.titleMedium)
        Text("Severity: ${snapshot.severity}", style = MaterialTheme.typography.bodyMedium)

        if (snapshot.timestamp == 0L) {
            Text("No inference has been run yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Timestamp: ${snapshot.timestamp}", style = MaterialTheme.typography.bodySmall)
            if (snapshot.summary.isNotBlank()) {
                Text("Summary: ${snapshot.summary}", style = MaterialTheme.typography.bodyMedium)
            }
            if (snapshot.actionToolNames.isNotEmpty()) {
                Text("Actions", style = MaterialTheme.typography.titleSmall)
                snapshot.actionToolNames.forEach { toolName ->
                    Text("- $toolName", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text("Actions: none", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
