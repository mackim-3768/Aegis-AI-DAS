package com.aegis.das.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.das.domain.scenario.ScenarioPresets
import com.aegis.das.ui.screens.tab1.Tab1Screen
import com.aegis.das.ui.screens.tab2.Tab2Screen
import com.aegis.das.ui.screens.tab3.Tab3Screen
import com.aegis.das.ui.screens.tab4.Tab4Screen

private data class TabItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val tabs = listOf(
        TabItem("Digital Twin", Icons.Outlined.DirectionsCar) { Tab1Screen(state) },
        TabItem("Neural Grid", Icons.Outlined.GridView) {
            Tab2Screen(
                state = state,
                onToolUpdated = { toolId, partial -> viewModel.updateTool(toolId, partial) }
            )
        },
        TabItem("Thinking Log", Icons.Outlined.Timeline) { Tab3Screen(state) },
        TabItem("System Control", Icons.Outlined.Settings) {
            Tab4Screen(
                state = state,
                onDebugToggle = viewModel::setDebugMode,
                onProcessorSelected = viewModel::setProcessor,
                scenarios = ScenarioPresets.all,
                onScenarioSelected = viewModel::applyScenario
            )
        }
    )
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tabs[selectedIndex].label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            tabs[selectedIndex].content()
        }
    }
}
