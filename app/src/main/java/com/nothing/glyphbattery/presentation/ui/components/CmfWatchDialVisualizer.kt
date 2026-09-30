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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import com.nothing.glyphbattery.domain.model.HeartRateZone
import com.nothing.glyphbattery.presentation.ui.theme.CmfDarkGray
import com.nothing.glyphbattery.presentation.ui.theme.CmfOrange
import com.nothing.glyphbattery.presentation.ui.theme.CmfOrangeGlow
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun CmfWatchDialVisualizer(
    metrics: CmfWatchMetrics,
    zone: HeartRateZone,
    onConnectClick: () -> Unit,
    onTriggerWristGesture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween((60000 / metrics.heartRateBpm.coerceAtLeast(40)), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CmfOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CMF WATCH PRO 2",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onConnectClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (metrics.isConnected) CmfOrange else NothingWhiteMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (metrics.isConnected) "PAIRED" else "SCANNING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (metrics.isConnected) CmfOrange else NothingWhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular CMF Watch Pro 2 Dial Canvas
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = 80.dp.toPx()

                    // Watch Aluminum Outer Case
                    drawCircle(
                        color = CmfDarkGray,
                        radius = radius + 14.dp.toPx(),
                        center = center
                    )

                    // CMF Functional Crown Knob (at 2 o'clock position)
                    drawRoundRect(
                        color = CmfOrange,
                        topLeft = Offset(center.x + radius + 8.dp.toPx(), center.y - 14.dp.toPx()),
                        size = Size(8.dp.toPx(), 28.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )

                    // Inner AMOLED Screen
                    drawCircle(
                        color = NothingBlack,
                        radius = radius,
                        center = center
                    )

                    // Heart Rate Circular Gauge Arc
                    val progressAngle = ((metrics.heartRateBpm - 40) / 160f).coerceIn(0f, 1f) * 270f
                    drawArc(
                        color = Color(0xFF222222),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius + 12.dp.toPx(), center.y - radius + 12.dp.toPx()),
                        size = Size((radius - 12.dp.toPx()) * 2, (radius - 12.dp.toPx()) * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = CmfOrange,
                        startAngle = 135f,
                        sweepAngle = progressAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius + 12.dp.toPx(), center.y - radius + 12.dp.toPx()),
                        size = Size((radius - 12.dp.toPx()) * 2, (radius - 12.dp.toPx()) * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Dial Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size((22 * pulseScale).dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${metrics.heartRateBpm}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite
                    )
                    Text(
                        text = "BPM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CmfOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Training Zone Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CmfOrange.copy(alpha = 0.15f))
                    .border(1.dp, CmfOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TRAINING ZONE: ${zone.zoneName.uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CmfOrangeGlow
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Watch Gesture / Find Trigger Button
            Button(
                onClick = { onTriggerWristGesture() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CmfDarkGray,
                    contentColor = NothingWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = CmfOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIMULATE WATCH GESTURE / FIND TRIGGER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
