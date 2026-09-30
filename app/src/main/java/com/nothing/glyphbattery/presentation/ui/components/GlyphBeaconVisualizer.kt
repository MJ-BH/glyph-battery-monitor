package com.nothing.glyphbattery.presentation.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.presentation.ui.theme.CmfOrange
import com.nothing.glyphbattery.presentation.ui.theme.GlyphGlowActive
import com.nothing.glyphbattery.presentation.ui.theme.GlyphInactive
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun GlyphBeaconVisualizer(
    session: BeaconSession,
    deviceModel: NothingDeviceModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOTHING GLYPH BEACON",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = deviceModel.displayName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NothingWhite
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingCardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val isIlluminated = session.currentStrobePulseAlpha > 0.1f || session.isBrakingDecelerating
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (session.isBrakingDecelerating) NothingRed
                                else if (isIlluminated) GlyphGlowActive
                                else NothingWhiteMuted
                            )
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (session.isBrakingDecelerating) "BRAKE LIGHT (100%)"
                        else if (isIlluminated) "STROBE ON"
                        else "IDLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isIlluminated) NothingWhite else NothingWhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phone Schematic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NothingBlack)
                    .border(1.dp, NothingCardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(190.dp, 210.dp)) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h * 0.42f

                    // Phone Body
                    drawRoundRect(
                        color = Color(0xFF151515),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(22f, 22f)
                    )
                    drawRoundRect(
                        color = Color(0xFF2A2A2A),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(22f, 22f),
                        style = Stroke(width = 2f)
                    )

                    // Center Dual Camera
                    drawCircle(color = Color(0xFF0C0C0C), radius = 46f, center = Offset(centerX, centerY))
                    drawCircle(color = Color(0xFF2E2E2E), radius = 46f, center = Offset(centerX, centerY), style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF000000), radius = 15f, center = Offset(centerX - 18f, centerY))
                    drawCircle(color = Color(0xFF000000), radius = 15f, center = Offset(centerX + 18f, centerY))
                    drawCircle(color = NothingRed, radius = 4f, center = Offset(centerX + 32f, centerY - 26f))

                    val alpha = if (session.isBrakingDecelerating) 1.0f else session.currentStrobePulseAlpha.coerceIn(0f, 1f)
                    val activeColor = if (session.isBrakingDecelerating) {
                        NothingRed.copy(alpha = 1.0f)
                    } else if (alpha > 0.05f) {
                        GlyphGlowActive.copy(alpha = alpha)
                    } else {
                        GlyphInactive
                    }

                    // Ribbon A (Top Left Arc)
                    drawArc(
                        color = activeColor,
                        startAngle = 140f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(centerX - 66f, centerY - 66f),
                        size = Size(132f, 132f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )

                    // Ribbon B (Top Right Arc)
                    drawArc(
                        color = activeColor,
                        startAngle = 330f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(centerX - 66f, centerY - 66f),
                        size = Size(132f, 132f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )

                    // Ribbon C (Right Vertical Strip)
                    drawLine(
                        color = activeColor,
                        start = Offset(centerX + 72f, centerY - 28f),
                        end = Offset(centerX + 72f, centerY + 28f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
