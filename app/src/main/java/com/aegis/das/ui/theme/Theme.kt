package com.aegis.das.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AegisPrimary,
    secondary = AegisSecondary,
    background = AegisBackground,
    surface = AegisSurface,
    error = AegisDanger,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AegisTextPrimary,
    onSurface = AegisTextPrimary,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = AegisPrimary,
    secondary = AegisSecondary,
    background = Color(0xFF0F1115),
    surface = Color(0xFF161A20),
    error = AegisDanger,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6E8EB),
    onSurface = Color(0xFFE6E8EB),
    onError = Color.White
)

@Composable
fun AegisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AegisShapes,
        content = content
    )
}
