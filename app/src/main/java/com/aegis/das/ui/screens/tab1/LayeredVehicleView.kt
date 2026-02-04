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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.tools.ToolId
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
internal fun LayeredVehicleView(
    state: AppState,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    val laneDeparture = state.contextStates[ToolId.GET_LANE_DEPARTURE_STATUS].bool("value")
    val forwardCollisionLevel = state.contextStates[ToolId.GET_FORWARD_COLLISION_RISK].string("level")
    val blindSpot = state.contextStates[ToolId.GET_BLIND_SPOT_COLLISION_RISK].bool("value")
    val blindSpotLevel = state.contextStates[ToolId.GET_BLIND_SPOT_COLLISION_RISK].string("level")

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

    val laneDepartureColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
    val forwardCollisionColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
    val blindSpotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
    val moodOverlayColor = if (moodPulse) {
        moodAnimated.copy(alpha = moodAlpha)
    } else {
        moodAnimated.copy(alpha = 0.2f)
    }

    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .border(width = 0.5.dp, color = borderColor, shape = cardShape)
            .padding(14.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawBase(viewMode)

            if (laneDeparture) {
                drawLaneDepartureOverlay(color = laneDepartureColor)
            }

            if (forwardCollisionLevel == "high" || forwardCollisionLevel == "critical") {
                drawForwardCollisionOverlay(color = forwardCollisionColor)
            }

            if (blindSpot || blindSpotLevel == "high") {
                drawBlindSpotOverlay(color = blindSpotColor)
            }

            drawMoodLightingOverlay(color = moodOverlayColor)
        }

        val amplitude = when (vibrationLevel) {
            "high" -> 6f
            "mid" -> 4f
            else -> 2f
        }

        if (viewMode == ViewMode.INTERIOR) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = if (vibrating) (shake * amplitude).dp else 0.dp)
            ) {
                Text("Steering", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (hudMessage.isNotBlank()) {
            val tone = when (hudLevel) {
                "danger" -> MaterialTheme.colorScheme.error
                "warning" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tone.copy(alpha = 0.15f))
                    .border(0.5.dp, tone.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(hudMessage, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun DrawScope.drawBase(viewMode: ViewMode) {
    val stroke = Stroke(width = 3f)
    val base = Color.Black.copy(alpha = 0.08f)
    val accent = Color.Black.copy(alpha = 0.14f)
    val glass = Color.Black.copy(alpha = 0.05f)

    val w = size.width
    val h = size.height

    when (viewMode) {
        ViewMode.TOP -> {
            val carWidth = w * 0.44f
            val carHeight = h * 0.78f
            val topLeft = Offset((w - carWidth) / 2f, (h - carHeight) / 2f)
            val corner = CornerRadius(minOf(w, h) * 0.14f, minOf(w, h) * 0.14f)

            drawRoundRect(
                color = base,
                topLeft = topLeft,
                size = Size(carWidth, carHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                color = accent,
                topLeft = topLeft,
                size = Size(carWidth, carHeight),
                cornerRadius = corner,
                style = stroke
            )

            val roofInsetX = carWidth * 0.14f
            val roofInsetY = carHeight * 0.14f
            val roofTopLeft = Offset(topLeft.x + roofInsetX, topLeft.y + roofInsetY)
            val roofSize = Size(carWidth - roofInsetX * 2f, carHeight * 0.46f)
            drawRoundRect(
                color = glass,
                topLeft = roofTopLeft,
                size = roofSize,
                cornerRadius = CornerRadius(corner.x * 0.75f, corner.y * 0.75f)
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.10f),
                topLeft = roofTopLeft,
                size = roofSize,
                cornerRadius = CornerRadius(corner.x * 0.75f, corner.y * 0.75f),
                style = Stroke(width = 2f)
            )

            val wheelWidth = carWidth * 0.18f
            val wheelHeight = carHeight * 0.12f
            val wheelCorner = CornerRadius(wheelHeight * 0.6f, wheelHeight * 0.6f)
            val wheelXs = listOf(topLeft.x - wheelWidth * 0.35f, topLeft.x + carWidth - wheelWidth * 0.65f)
            val wheelYs = listOf(topLeft.y + carHeight * 0.18f, topLeft.y + carHeight * 0.70f)
            wheelXs.forEach { wx ->
                wheelYs.forEach { wy ->
                    drawRoundRect(
                        color = accent.copy(alpha = 0.22f),
                        topLeft = Offset(wx, wy),
                        size = Size(wheelWidth, wheelHeight),
                        cornerRadius = wheelCorner
                    )
                }
            }

            val centerX = w / 2f
            drawLine(
                color = accent.copy(alpha = 0.10f),
                start = Offset(centerX, topLeft.y + carHeight * 0.10f),
                end = Offset(centerX, topLeft.y + carHeight * 0.90f),
                strokeWidth = 2f
            )

            val headLightR = minOf(w, h) * 0.018f
            drawCircle(
                color = accent.copy(alpha = 0.16f),
                radius = headLightR,
                center = Offset(topLeft.x + carWidth * 0.30f, topLeft.y + carHeight * 0.06f)
            )
            drawCircle(
                color = accent.copy(alpha = 0.16f),
                radius = headLightR,
                center = Offset(topLeft.x + carWidth * 0.70f, topLeft.y + carHeight * 0.06f)
            )
        }

        ViewMode.INTERIOR -> {
            val cabinWidth = w * 0.86f
            val cabinHeight = h * 0.56f
            val topLeft = Offset((w - cabinWidth) / 2f, (h - cabinHeight) / 2f)
            val corner = CornerRadius(minOf(w, h) * 0.10f, minOf(w, h) * 0.10f)

            drawRoundRect(
                color = base,
                topLeft = topLeft,
                size = Size(cabinWidth, cabinHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                color = accent,
                topLeft = topLeft,
                size = Size(cabinWidth, cabinHeight),
                cornerRadius = corner,
                style = stroke
            )

            val dashHeight = cabinHeight * 0.18f
            drawRoundRect(
                color = accent.copy(alpha = 0.10f),
                topLeft = Offset(topLeft.x + cabinWidth * 0.08f, topLeft.y + cabinHeight * 0.08f),
                size = Size(cabinWidth * 0.84f, dashHeight),
                cornerRadius = CornerRadius(dashHeight * 0.6f, dashHeight * 0.6f)
            )

            val seatWidth = cabinWidth * 0.26f
            val seatHeight = cabinHeight * 0.46f
            val seatY = topLeft.y + cabinHeight * 0.34f
            val seatCorner = CornerRadius(seatWidth * 0.25f, seatWidth * 0.25f)

            drawRoundRect(
                color = accent.copy(alpha = 0.14f),
                topLeft = Offset(topLeft.x + cabinWidth * 0.18f, seatY),
                size = Size(seatWidth, seatHeight),
                cornerRadius = seatCorner
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.14f),
                topLeft = Offset(topLeft.x + cabinWidth * 0.56f, seatY),
                size = Size(seatWidth, seatHeight),
                cornerRadius = seatCorner
            )

            val wheelCenter = Offset(topLeft.x + cabinWidth * 0.33f, topLeft.y + cabinHeight * 0.36f)
            val wheelRadius = minOf(w, h) * 0.10f
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = wheelRadius,
                center = wheelCenter,
                style = Stroke(width = 8f)
            )
            drawCircle(
                color = accent.copy(alpha = 0.10f),
                radius = wheelRadius * 0.45f,
                center = wheelCenter
            )

            drawRoundRect(
                color = glass,
                topLeft = Offset(topLeft.x + cabinWidth * 0.40f, topLeft.y + cabinHeight * 0.22f),
                size = Size(cabinWidth * 0.20f, cabinHeight * 0.56f),
                cornerRadius = CornerRadius(corner.x * 0.6f, corner.y * 0.6f)
            )
        }
    }
}

private fun DrawScope.drawLaneDepartureOverlay(color: Color) {
    val w = size.width
    val h = size.height

    val leftX = w * 0.25f
    val rightX = w * 0.75f
    val top = h * 0.12f
    val bottom = h * 0.88f

    drawLine(color = color, start = Offset(leftX, top), end = Offset(leftX, bottom), strokeWidth = 8f)
    drawLine(color = color, start = Offset(rightX, top), end = Offset(rightX, bottom), strokeWidth = 8f)
}

private fun DrawScope.drawForwardCollisionOverlay(color: Color) {
    val w = size.width
    val h = size.height

    val centerX = w / 2f
    val top = h * 0.12f

    val p1 = Offset(centerX, top)
    val p2 = Offset(centerX - w * 0.08f, top + h * 0.12f)
    val p3 = Offset(centerX + w * 0.08f, top + h * 0.12f)

    drawLine(color, p1, p2, strokeWidth = 10f)
    drawLine(color, p2, p3, strokeWidth = 10f)
    drawLine(color, p3, p1, strokeWidth = 10f)
}

private fun DrawScope.drawBlindSpotOverlay(color: Color) {
    val w = size.width
    val h = size.height

    val y = h * 0.62f
    val r = minOf(w, h) * 0.05f

    drawCircle(color = color, radius = r, center = Offset(w * 0.2f, y))
    drawCircle(color = color, radius = r, center = Offset(w * 0.8f, y))
}

private fun DrawScope.drawMoodLightingOverlay(color: Color) {
    val w = size.width
    val h = size.height
    val overlayHeight = h * 0.22f
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.12f, h - overlayHeight - h * 0.08f),
        size = Size(w * 0.76f, overlayHeight),
        cornerRadius = CornerRadius(60f, 60f)
    )
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
