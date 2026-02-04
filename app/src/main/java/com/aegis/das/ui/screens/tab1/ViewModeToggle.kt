package com.aegis.das.ui.screens.tab1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ViewModeToggle(
    viewMode: ViewMode,
    onViewModeChanged: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = viewMode == ViewMode.TOP,
            onClick = { onViewModeChanged(ViewMode.TOP) },
            label = { Text("Top", style = MaterialTheme.typography.labelLarge) }
        )
        FilterChip(
            selected = viewMode == ViewMode.INTERIOR,
            onClick = { onViewModeChanged(ViewMode.INTERIOR) },
            label = { Text("Interior", style = MaterialTheme.typography.labelLarge) }
        )
    }
}
