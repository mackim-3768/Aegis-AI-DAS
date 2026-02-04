package com.aegis.das.ui.screens.tab1

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
fun Tab1Screen(state: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aegis Digital Twin", style = MaterialTheme.typography.headlineSmall)
        Text("Layered View placeholder", style = MaterialTheme.typography.bodyMedium)
        Text("Debug: ${state.debugMode}", style = MaterialTheme.typography.labelMedium)
    }
}
