package com.aegis.das.ui.screens.tab2

import androidx.compose.runtime.Composable
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.tools.ToolId

@Composable
fun Tab2Screen(
    state: AppState,
    onToolUpdated: (ToolId, Map<String, Any?>) -> Unit
) {
    NeuralGridScreen(state = state, onToolUpdated = onToolUpdated)
}

