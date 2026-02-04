package com.aegis.das.ui.screens.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.tools.ToolKind

@Composable
internal fun ToolKindFilterRow(
    selected: ToolKind,
    onSelected: (ToolKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChip(
            selected = selected == ToolKind.CONTEXT,
            onClick = { onSelected(ToolKind.CONTEXT) },
            label = { Text("Context Tools", style = MaterialTheme.typography.labelLarge) }
        )
        FilterChip(
            selected = selected == ToolKind.ACTION,
            onClick = { onSelected(ToolKind.ACTION) },
            label = { Text("Action Tools", style = MaterialTheme.typography.labelLarge) }
        )
    }
}
