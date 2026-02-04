package com.aegis.das.ui.screens.tab1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
internal fun DigitalTwinScreen(state: AppState) {
    var viewMode by remember { mutableStateOf(ViewMode.TOP) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewModeToggle(viewMode = viewMode, onViewModeChanged = { viewMode = it })
            Text(
                text = "Debug: ${state.debugMode}",
                style = MaterialTheme.typography.labelMedium
            )
        }

        LayeredVehicleView(
            state = state,
            viewMode = viewMode,
            modifier = Modifier.fillMaxSize()
        )
    }
}
