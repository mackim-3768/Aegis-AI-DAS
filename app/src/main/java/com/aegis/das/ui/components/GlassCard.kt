package com.aegis.das.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    contentPadding: Modifier = Modifier.padding(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(width = 0.5.dp, color = borderColor),
        tonalElevation = 2.dp
    ) {
        Column(modifier = contentPadding, content = content)
    }
}
