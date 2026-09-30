package com.nothing.glyphbattery.presentation.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.presentation.ui.theme.CmfOrange
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun BrakeSpeedCard(
    speedKmh: Float,
    isBraking: Boolean,
    onSimulateSpeedChange: (Float, Boolean) -> Unit,
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
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = CmfOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "  BIKE & MOTION TELEMETRY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 1.sp
                    )
                }

                if (isBraking) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NothingRed)
                        )
                        Text(
                            text = " BRAKING",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingRed
                        )
                    }
                }
            }

            // Speedometer Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = String.format("%.1f", speedKmh),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite
                )
                Text(
                    text = " KM/H",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CmfOrange,
                    modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                )
            }

            // Speed Simulation Slider
            Column {
                Text(
                    text = "SIMULATE CYCLING / RUNNING SPEED",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NothingWhiteMuted
                )
                Slider(
                    value = speedKmh,
                    onValueChange = { onSimulateSpeedChange(it, false) },
                    valueRange = 0f..45f,
                    colors = SliderDefaults.colors(
                        thumbColor = CmfOrange,
                        activeTrackColor = CmfOrange,
                        inactiveTrackColor = NothingCardBorder
                    )
                )
            }

            // Instant Brake Trigger Button
            Button(
                onClick = { onSimulateSpeedChange(0f, !isBraking) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBraking) NothingRed else NothingCardBorder,
                    contentColor = NothingWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isBraking) "  RELEASE BRAKE LIGHT" else "  TRIGGER DECELERATION BRAKE LIGHT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
