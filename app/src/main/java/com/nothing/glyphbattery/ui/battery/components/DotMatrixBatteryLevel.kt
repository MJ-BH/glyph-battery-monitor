package com.nothing.glyphbattery.ui.battery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.ui.theme.NothingRed
import com.nothing.glyphbattery.ui.theme.NothingWhite
import com.nothing.glyphbattery.ui.theme.NothingWhiteMuted

@Composable
fun DotMatrixBatteryLevel(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Charging Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCharging) NothingRed.copy(alpha = 0.2f) else NothingCardBorder)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isCharging) NothingRed else NothingWhiteMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCharging) "CHARGING" else "DISCHARGING",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCharging) NothingRed else NothingWhiteMuted,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Large Digits
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$level",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingRed,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dotted Progress Meter
            DottedProgressBar(
                progress = level / 100f,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(12.dp)
            )
        }
    }
}

@Composable
fun DottedProgressBar(
    progress: Float,
    dotCount: Int = 20,
    activeColor: Color = NothingWhite,
    inactiveColor: Color = NothingCardBorder,
    modifier: Modifier = Modifier
) {
    val activeDots = (progress.coerceIn(0f, 1f) * dotCount).toInt()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until dotCount) {
            val isActive = i < activeDots
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (i % 5 == 4) 10.dp else 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else inactiveColor)
            )
        }
    }
}
