package com.nothing.glyphbattery.presentation.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.presentation.ui.components.BatteryStatusMetricsGrid
import com.nothing.glyphbattery.presentation.ui.components.DotMatrixBatteryLevel
import com.nothing.glyphbattery.presentation.ui.components.GlyphDeviceVisualizer
import com.nothing.glyphbattery.presentation.ui.components.NothingHeader
import com.nothing.glyphbattery.presentation.ui.theme.GlyphGlowActive
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun BatteryDashboardScreen(
    state: BatteryDashboardState,
    onEvent: (BatteryDashboardEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(BatteryDashboardEvent.ClearFeedbackMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NothingBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item(key = "header") {
                NothingHeader(
                    currentModel = state.glyphState.deviceModel,
                    isGlyphConnected = state.glyphState.isConnected,
                    onModelSelected = { onEvent(BatteryDashboardEvent.SelectDeviceModel(it)) }
                )
            }

            // Battery Level Display
            item(key = "battery_meter") {
                DotMatrixBatteryLevel(
                    level = state.batteryInfo.level,
                    isCharging = state.batteryInfo.isCharging,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Flash Action Button
            item(key = "action_flash") {
                Button(
                    onClick = { onEvent(BatteryDashboardEvent.FlashGlyphBattery) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isFlashingNow) NothingWhite else NothingRed,
                        contentColor = if (state.isFlashingNow) NothingBlack else NothingWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isFlashingNow) "FLASHING GLYPH..." else "FLASH BATTERY ON GLYPH",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Glyph LED Visualizer
            item(key = "glyph_visualizer") {
                GlyphDeviceVisualizer(
                    glyphState = state.glyphState,
                    batteryLevel = state.batteryInfo.level,
                    isCharging = state.batteryInfo.isCharging
                )
            }

            // Glyph Settings & Controls Card
            item(key = "glyph_controls") {
                GlyphControlPanel(
                    state = state,
                    onEvent = onEvent
                )
            }

            // Battery Diagnostics Metrics Grid
            item(key = "battery_metrics") {
                BatteryStatusMetricsGrid(
                    batteryInfo = state.batteryInfo
                )
            }

            // Footer note
            item(key = "footer") {
                Text(
                    text = "NOTHING GLYPH DEVELOPER KIT INTEGRATION\nSupports Phone (1), Phone (2), Phone (2a), Phone (2a) Plus & 3a Series",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NothingWhiteMuted.copy(alpha = 0.6f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun GlyphControlPanel(
    state: BatteryDashboardState,
    onEvent: (BatteryDashboardEvent) -> Unit,
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NothingRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GLYPH LIGHTING MODES",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite,
                    letterSpacing = 1.sp
                )
            }

            // Mode Selector Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlyphAnimationMode.entries.forEach { mode ->
                    val isSelected = state.glyphState.activeMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) NothingCardBorder else Color(0xFF181818))
                            .border(
                                1.dp,
                                if (isSelected) NothingWhite.copy(alpha = 0.4f) else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onEvent(BatteryDashboardEvent.ChangeAnimationMode(mode)) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.title,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) NothingWhite else NothingWhiteMuted
                            )
                            Text(
                                text = mode.description,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = NothingWhiteMuted.copy(alpha = 0.8f)
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Switch: Sync with Charging
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync Glow with Charging",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NothingWhite
                    )
                    Text(
                        text = "Breathing pulse while phone is plugged in",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingWhiteMuted
                    )
                }
                Switch(
                    checked = state.glyphState.syncWithCharging,
                    onCheckedChange = { onEvent(BatteryDashboardEvent.ToggleChargingGlow(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = NothingRed,
                        uncheckedThumbColor = NothingWhiteMuted,
                        uncheckedTrackColor = NothingBlack
                    )
                )
            }

            // Switch: Flash on Plug-in
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flash Battery on Cable Connect",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NothingWhite
                    )
                    Text(
                        text = "Briefly displays battery level when plugged in",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingWhiteMuted
                    )
                }
                Switch(
                    checked = state.glyphState.flashOnPlugIn,
                    onCheckedChange = { onEvent(BatteryDashboardEvent.ToggleFlashOnPlugIn(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = NothingRed,
                        uncheckedThumbColor = NothingWhiteMuted,
                        uncheckedTrackColor = NothingBlack
                    )
                )
            }
        }
    }
}
