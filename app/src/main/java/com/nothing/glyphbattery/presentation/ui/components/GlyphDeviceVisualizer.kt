package com.nothing.glyphbattery.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.data.glyph.NothingGlyphConstants
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.presentation.ui.theme.GlyphGlowActive
import com.nothing.glyphbattery.presentation.ui.theme.GlyphInactive
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun GlyphDeviceVisualizer(
    glyphState: GlyphState,
    batteryLevel: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glyph_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Model Info & Active LED Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GLYPH INTERFACE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = glyphState.deviceModel.displayName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NothingWhite
                    )
                }

                // LED Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingCardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val isLit = glyphState.activeChannels.isNotEmpty() || (isCharging && glyphState.syncWithCharging)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLit) GlyphGlowActive else NothingWhiteMuted)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (isLit) "LEDS ACTIVE" else "STANDBY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isLit) NothingWhite else NothingWhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Schematic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NothingBlack)
                    .border(1.dp, NothingCardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (glyphState.deviceModel) {
                    NothingDeviceModel.PHONE_2A,
                    NothingDeviceModel.PHONE_2A_PLUS,
                    NothingDeviceModel.PHONE_3A_SERIES -> {
                        NothingPhone2aCanvas(
                            glyphState = glyphState,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            pulseAlpha = pulseGlow
                        )
                    }
                    NothingDeviceModel.PHONE_2 -> {
                        NothingPhone2Canvas(
                            glyphState = glyphState,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            pulseAlpha = pulseGlow
                        )
                    }
                    else -> {
                        NothingPhone1Canvas(
                            glyphState = glyphState,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            pulseAlpha = pulseGlow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Status readout
            Text(
                text = "Active Channels: ${
                    if (glyphState.activeChannels.isEmpty()) "None"
                    else glyphState.activeChannels.joinToString(", ")
                }",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = NothingWhiteMuted
            )
        }
    }
}

@Composable
private fun NothingPhone2aCanvas(
    glyphState: GlyphState,
    batteryLevel: Int,
    isCharging: Boolean,
    pulseAlpha: Float
) {
    Canvas(modifier = Modifier.size(200.dp, 220.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h * 0.42f

        // Phone Silhouette Outline
        drawRoundRect(
            color = Color(0xFF161616),
            size = Size(w, h),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = Color(0xFF2E2E2E),
            size = Size(w, h),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 2f)
        )

        // Center Camera "Eyes" Module (Phone 2a signature)
        drawCircle(
            color = Color(0xFF0D0D0D),
            radius = 48f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFF333333),
            radius = 48f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )
        // Left camera lens
        drawCircle(
            color = Color(0xFF000000),
            radius = 16f,
            center = Offset(centerX - 20f, centerY)
        )
        drawCircle(
            color = Color(0xFF1E88E5),
            radius = 6f,
            center = Offset(centerX - 20f, centerY)
        )
        // Right camera lens
        drawCircle(
            color = Color(0xFF000000),
            radius = 16f,
            center = Offset(centerX + 20f, centerY)
        )
        drawCircle(
            color = Color(0xFF1E88E5),
            radius = 6f,
            center = Offset(centerX + 20f, centerY)
        )

        // Nothing Red dot accent
        drawCircle(
            color = NothingRed,
            radius = 4f,
            center = Offset(centerX + 34f, centerY - 28f)
        )

        // Strip A: Top Left Arc
        val isAActive = glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_A) ||
                (isCharging && glyphState.syncWithCharging) ||
                (batteryLevel > 10 && glyphState.activeMode == GlyphAnimationMode.PROGRESS_BAR)

        val alphaA = if (isCharging) pulseAlpha else if (isAActive) 1f else 0.2f
        val colorA = if (isAActive) GlyphGlowActive.copy(alpha = alphaA) else GlyphInactive

        drawArc(
            color = colorA,
            startAngle = 140f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(centerX - 70f, centerY - 70f),
            size = Size(140f, 140f),
            style = Stroke(width = 7f, cap = StrokeCap.Round)
        )

        // Strip B: Top Right Arc
        val isBActive = glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_B) ||
                (isCharging && glyphState.syncWithCharging) ||
                (batteryLevel > 45 && glyphState.activeMode == GlyphAnimationMode.PROGRESS_BAR)

        val alphaB = if (isCharging) pulseAlpha else if (isBActive) 1f else 0.2f
        val colorB = if (isBActive) GlyphGlowActive.copy(alpha = alphaB) else GlyphInactive

        drawArc(
            color = colorB,
            startAngle = 330f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(centerX - 70f, centerY - 70f),
            size = Size(140f, 140f),
            style = Stroke(width = 7f, cap = StrokeCap.Round)
        )

        // Strip C: Right Vertical Ribbon Strip
        val isCActive = glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_C) ||
                (isCharging && glyphState.syncWithCharging) ||
                (batteryLevel > 80 && glyphState.activeMode == GlyphAnimationMode.PROGRESS_BAR)

        val alphaC = if (isCharging) pulseAlpha else if (isCActive) 1f else 0.2f
        val colorC = if (isCActive) GlyphGlowActive.copy(alpha = alphaC) else GlyphInactive

        drawLine(
            color = colorC,
            start = Offset(centerX + 76f, centerY - 30f),
            end = Offset(centerX + 76f, centerY + 30f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        // Bottom Cable Ribbon Graphic (Phone 2a lower internal ribbon)
        val ribbonPath = Path().apply {
            moveTo(centerX - 35f, centerY + 70f)
            lineTo(centerX - 35f, h - 30f)
            lineTo(centerX + 35f, h - 30f)
            lineTo(centerX + 35f, centerY + 85f)
        }
        drawPath(
            path = ribbonPath,
            color = Color(0xFF222222),
            style = Stroke(width = 3f)
        )
    }
}

@Composable
private fun NothingPhone2Canvas(
    glyphState: GlyphState,
    batteryLevel: Int,
    isCharging: Boolean,
    pulseAlpha: Float
) {
    Canvas(modifier = Modifier.size(200.dp, 220.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h * 0.48f

        // Phone Outline
        drawRoundRect(
            color = Color(0xFF161616),
            size = Size(w, h),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = Color(0xFF2E2E2E),
            size = Size(w, h),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 2f)
        )

        // Central Wireless Charging Coil (C1 - C16 segments)
        val isCenterActive = glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_C1_PROGRESS) ||
                (isCharging && glyphState.syncWithCharging) ||
                glyphState.activeMode == GlyphAnimationMode.PROGRESS_BAR

        val centerAlpha = if (isCharging) pulseAlpha else if (isCenterActive) 1f else 0.2f
        val centerColor = if (isCenterActive) GlyphGlowActive.copy(alpha = centerAlpha) else GlyphInactive

        drawCircle(
            color = centerColor,
            radius = 50f,
            center = Offset(centerX, centerY),
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 6f), 0f)
            )
        )

        // D1 Bottom Exclamation Mark / Progress Indicator
        val d1Height = (batteryLevel / 100f) * 40f
        val isD1Active = batteryLevel > 5 || isCharging
        val d1Color = if (isD1Active) GlyphGlowActive.copy(alpha = if (isCharging) pulseAlpha else 1f) else GlyphInactive

        drawLine(
            color = d1Color,
            start = Offset(centerX, h - 25f),
            end = Offset(centerX, h - 25f - d1Height.coerceAtLeast(8f)),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Top Left Diagonal (Channel A)
        drawLine(
            color = if (glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_A)) GlyphGlowActive else GlyphInactive,
            start = Offset(centerX - 55f, 30f),
            end = Offset(centerX - 15f, 30f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun NothingPhone1Canvas(
    glyphState: GlyphState,
    batteryLevel: Int,
    isCharging: Boolean,
    pulseAlpha: Float
) {
    Canvas(modifier = Modifier.size(200.dp, 220.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h * 0.48f

        // Phone Outline
        drawRoundRect(
            color = Color(0xFF161616),
            size = Size(w, h),
            cornerRadius = CornerRadius(24f, 24f)
        )

        // Top Camera Ring
        drawCircle(
            color = GlyphInactive,
            radius = 28f,
            center = Offset(centerX - 35f, 50f),
            style = Stroke(width = 5f)
        )

        // Center Big Ring
        val isCenterActive = isCharging || glyphState.activeChannels.contains(NothingGlyphConstants.CHANNEL_C)
        val centerAlpha = if (isCharging) pulseAlpha else if (isCenterActive) 1f else 0.25f
        drawCircle(
            color = if (isCenterActive) GlyphGlowActive.copy(alpha = centerAlpha) else GlyphInactive,
            radius = 45f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 5f)
        )

        // Bottom D1 Progress Indicator
        val d1Height = (batteryLevel / 100f) * 35f
        drawLine(
            color = if (batteryLevel > 5) GlyphGlowActive.copy(alpha = if (isCharging) pulseAlpha else 1f) else GlyphInactive,
            start = Offset(centerX, h - 25f),
            end = Offset(centerX, h - 25f - d1Height.coerceAtLeast(8f)),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}
