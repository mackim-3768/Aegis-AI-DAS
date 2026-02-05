package com.aegis.das.ui.screens.tab1

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.tools.ToolId
import kotlinx.coroutines.delay
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import kotlin.math.max

@Composable
internal fun LayeredVehicleView(
    state: AppState,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    val vibrationTool = state.actionStates[ToolId.TRIGGER_STEERING_VIBRATION]
    val vibrationLevel = vibrationTool.string("level")
    val vibrationDurationMs = vibrationTool.int("duration_ms")

    val hazardTool = state.actionStates[ToolId.ACTIVATE_HAZARD_WARNING_SIGNALS]
    val hazardEnabled = hazardTool.bool("enabled")
    val hazardDurationMs = hazardTool.int("duration_ms")

    val moodTool = state.actionStates[ToolId.UPDATE_AMBIENT_MOOD_LIGHTING]
    val moodLevel = moodTool.string("level")
    val moodPulse = moodTool.bool("pulse")

    val hudTool = state.actionStates[ToolId.TRIGGER_HUD_WARNING]
    val hudMessage = hudTool.string("message")
    val hudLevel = hudTool.string("level")

    val clusterTool = state.actionStates[ToolId.TRIGGER_CLUSTER_VISUAL_WARNING]
    val clusterMessage = clusterTool.string("message")
    val clusterLevel = clusterTool.string("level")

    val navigationTool = state.actionStates[ToolId.TRIGGER_NAVIGATION_NOTIFICATION]
    val navigationMessage = navigationTool.string("message")
    val navigationLevel = navigationTool.string("level")

    val voiceTool = state.actionStates[ToolId.TRIGGER_VOICE_PROMPT]
    val voiceMessage = voiceTool.string("message")
    val voiceLevel = voiceTool.string("level")

    val restTool = state.actionStates[ToolId.TRIGGER_REST_RECOMMENDATION]
    val restReason = restTool.string("reason")
    val restLevel = restTool.string("level")

    val projectionTool = state.actionStates[ToolId.TRIGGER_GROUND_PROJECTION_WARNING]
    val projectionMessage = projectionTool.string("message")
    val projectionLevel = projectionTool.string("level")

    val logTool = state.actionStates[ToolId.LOG_SAFETY_EVENT]
    val logMessage = logTool.string("message")
    val logType = logTool.string("event_type")
    val logLevel = logTool.string("level")

    var vibrating by remember { mutableStateOf(false) }
    LaunchedEffect(vibrationTool?.updatedAt) {
        val duration = max(0, vibrationDurationMs)
        if (duration > 0 && vibrationLevel.isNotBlank()) {
            vibrating = true
            delay(duration.toLong())
            vibrating = false
        }
    }

    var hazardBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(hazardTool?.updatedAt) {
        val duration = if (hazardDurationMs > 0) hazardDurationMs else 1500
        if (hazardEnabled) {
            hazardBlinking = true
            delay(duration.toLong())
            hazardBlinking = false
        } else {
            hazardBlinking = false
        }
    }

    val infinite = rememberInfiniteTransition(label = "digital_twin")
    val blinkAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val shake by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val floatOffset by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val handleGlow by infinite.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handle_glow"
    )

    val moodColor = when (moodLevel) {
        "warning" -> MaterialTheme.colorScheme.tertiary
        "danger" -> MaterialTheme.colorScheme.error
        "safe" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    val moodAnimated by animateColorAsState(
        targetValue = moodColor,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "mood"
    )

    val moodAlpha by infinite.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mood_alpha"
    )

    val borderAlpha = if (hazardBlinking) blinkAlpha else 0.18f
    val borderColor = if (hazardBlinking) MaterialTheme.colorScheme.error.copy(alpha = borderAlpha)
    else MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)

    val moodOverlayColor = if (moodPulse) {
        moodAnimated.copy(alpha = moodAlpha)
    } else {
        moodAnimated.copy(alpha = 0.2f)
    }

    val vibrationAmplitude = when (vibrationLevel) {
        "high" -> 3.5f
        "mid" -> 2.5f
        else -> 1.5f
    }

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(24f, 24f, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        Modifier
    }

    val actionBanners = buildList {
        if (clusterMessage.isNotBlank()) {
            add(ActionBanner("Cluster", clusterMessage, clusterLevel))
        }
        if (navigationMessage.isNotBlank()) {
            add(ActionBanner("Navigation", navigationMessage, navigationLevel))
        }
        if (projectionMessage.isNotBlank()) {
            add(ActionBanner("Projection", projectionMessage, projectionLevel))
        }
        if (voiceMessage.isNotBlank()) {
            add(ActionBanner("Voice", voiceMessage, voiceLevel))
        }
        if (restReason.isNotBlank()) {
            add(ActionBanner("Rest", restReason, restLevel))
        }
        if (logMessage.isNotBlank() || logType.isNotBlank()) {
            val message = listOf(logType, logMessage).filter { it.isNotBlank() }.joinToString(" • ")
            add(ActionBanner("Log", message, logLevel))
        }
    }

    VehicleGlassCard(
        modifier = modifier,
        borderColor = borderColor,
        moodOverlayColor = moodOverlayColor,
        blurModifier = blurModifier
    ) {
        when (viewMode) {
            ViewMode.TOP -> {
                VehicleTopView(
                    modifier = Modifier.align(Alignment.Center),
                    floatOffset = floatOffset
                )
            }

            ViewMode.INTERIOR -> {
                VehicleInteriorView(
                    modifier = Modifier.align(Alignment.Center),
                    vibrating = vibrating,
                    shake = shake,
                    amplitude = vibrationAmplitude,
                    glowAlpha = if (vibrating) handleGlow else 0.18f
                )
            }
        }

        if (actionBanners.isNotEmpty()) {
            ActionBannerStack(
                banners = actionBanners,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            )
        }

        if (hudMessage.isNotBlank()) {
            HudWarningBanner(
                message = hudMessage,
                level = hudLevel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
            )
        }
    }
}

private data class ActionBanner(
    val title: String,
    val message: String,
    val level: String
)

@Composable
private fun VehicleGlassCard(
    modifier: Modifier,
    borderColor: Color,
    moodOverlayColor: Color,
    blurModifier: Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .shadow(8.dp, cardShape)
            .clip(cardShape)
            .background(Color.White.copy(alpha = 0.8f))
            .border(width = 0.5.dp, color = borderColor, shape = cardShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.70f),
                            Color.White.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(moodOverlayColor)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
private fun VehicleTopView(
    modifier: Modifier,
    floatOffset: Float
) {
    Column(
        modifier = modifier.offset(y = (floatOffset * 6f).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsCar,
                contentDescription = "Vehicle",
                modifier = Modifier.size(140.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun VehicleInteriorView(
    modifier: Modifier,
    vibrating: Boolean,
    shake: Float,
    amplitude: Float,
    glowAlpha: Float
) {
    Box(
        modifier = modifier
            .offset(
                x = if (vibrating) (shake * amplitude).dp else 0.dp,
                y = if (vibrating) (shake * amplitude * 0.6f).dp else 0.dp
            )
            .size(180.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    width = 8.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
                .border(
                    width = 10.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                    shape = CircleShape
                )
        )

        Text(
            text = "Handle",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun ActionBannerStack(
    banners: List<ActionBanner>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        banners.forEach { banner ->
            ActionBannerChip(banner)
        }
    }
}

@Composable
private fun ActionBannerChip(banner: ActionBanner) {
    val tone = when (banner.level) {
        "danger", "critical", "high" -> MaterialTheme.colorScheme.error
        "warning", "mid" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tone.copy(alpha = 0.12f))
            .border(0.5.dp, tone.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${banner.title}: ${banner.message}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun HudWarningBanner(
    message: String,
    level: String,
    modifier: Modifier = Modifier
) {
    val tone = when (level) {
        "danger" -> MaterialTheme.colorScheme.error
        "warning" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tone.copy(alpha = 0.14f))
            .border(0.5.dp, tone.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodySmall)
    }
}

private fun Map<String, Any?>.string(key: String): String = this[key] as? String ?: ""
private fun Map<String, Any?>.int(key: String): Int = when (val v = this[key]) {
    is Int -> v
    is Long -> v.toInt()
    is Float -> v.toInt()
    is Double -> v.toInt()
    else -> 0
}
private fun Map<String, Any?>.bool(key: String): Boolean = this[key] as? Boolean ?: false

private fun com.aegis.das.domain.state.ToolState?.string(key: String): String = this?.payload?.string(key).orEmpty()
private fun com.aegis.das.domain.state.ToolState?.int(key: String): Int = this?.payload?.int(key) ?: 0
private fun com.aegis.das.domain.state.ToolState?.bool(key: String): Boolean = this?.payload?.bool(key) ?: false
