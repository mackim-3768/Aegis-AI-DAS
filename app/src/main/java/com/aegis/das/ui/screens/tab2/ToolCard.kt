package com.aegis.das.ui.screens.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.ToolState
import com.aegis.das.domain.tools.ToolKind
import com.aegis.das.ui.components.GlassCard

@Composable
internal fun ToolCard(
    toolState: ToolState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryKey = when {
        "value" in toolState.payload -> "value"
        "level" in toolState.payload -> "level"
        "enabled" in toolState.payload -> "enabled"
        else -> toolState.payload.keys.firstOrNull()
    }
    val primaryValue = primaryKey?.let { toolState.payload[it] }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = if (enabled) onClick else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape),
                    color = when (toolState.id.kind) {
                        ToolKind.CONTEXT -> MaterialTheme.colorScheme.primary
                        ToolKind.ACTION -> MaterialTheme.colorScheme.tertiary
                    }
                ) {}

                Text(toolState.id.toolName, style = MaterialTheme.typography.titleSmall)
            }

            if (primaryKey != null) {
                Text(
                    text = "$primaryKey: $primaryValue",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "updatedAt=${toolState.updatedAt}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
