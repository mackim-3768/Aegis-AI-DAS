package com.aegis.das.ui.screens.tab4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.scenario.ScenarioPreset
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.ProcessorType

@Composable
fun Tab4Screen(
    state: AppState,
    onDebugToggle: (Boolean) -> Unit,
    onProcessorSelected: (ProcessorType) -> Unit,
    scenarios: List<ScenarioPreset>,
    onScenarioSelected: (ScenarioPreset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("System Control", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Debug Mode", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = state.debugMode, onCheckedChange = onDebugToggle)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Processor", style = MaterialTheme.typography.bodyMedium)
            ProcessorOption("CPU", ProcessorType.CPU, state.processor, onProcessorSelected)
            ProcessorOption("GPU", ProcessorType.GPU, state.processor, onProcessorSelected)
            ProcessorOption("NPU", ProcessorType.NPU, state.processor, onProcessorSelected)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Scenario Injector", style = MaterialTheme.typography.bodyMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(scenarios) { scenario ->
                    Button(onClick = { onScenarioSelected(scenario) }, modifier = Modifier.width(180.dp)) {
                        Text(scenario.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessorOption(
    label: String,
    value: ProcessorType,
    selected: ProcessorType,
    onSelected: (ProcessorType) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = value == selected, onClick = { onSelected(value) })
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
