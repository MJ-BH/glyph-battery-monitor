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
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Highlight
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
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.presentation.ui.components.BrakeSpeedCard
import com.nothing.glyphbattery.presentation.ui.components.CmfWatchDialVisualizer
import com.nothing.glyphbattery.presentation.ui.components.GlyphBeaconVisualizer
import com.nothing.glyphbattery.presentation.ui.components.MorseStudioCard
import com.nothing.glyphbattery.presentation.ui.components.NothingHeader
import com.nothing.glyphbattery.presentation.ui.theme.CmfOrange
import com.nothing.glyphbattery.presentation.ui.theme.NothingBlack
import com.nothing.glyphbattery.presentation.ui.theme.NothingCardBorder
import com.nothing.glyphbattery.presentation.ui.theme.NothingDarkSurface
import com.nothing.glyphbattery.presentation.ui.theme.NothingRed
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhite
import com.nothing.glyphbattery.presentation.ui.theme.NothingWhiteMuted

@Composable
fun BeaconCompanionScreen(
    state: BeaconCompanionState,
    onEvent: (BeaconCompanionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(BeaconCompanionEvent.ClearFeedback)
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
                    currentModel = state.deviceModel,
                    isGlyphConnected = true,
                    onModelSelected = { onEvent(BeaconCompanionEvent.SelectDeviceModel(it)) }
                )
            }

            // Mode Selector Bar
            item(key = "mode_selector") {
                ModeSelectorGrid(
                    activeMode = state.session.activeMode,
                    onSelectMode = { onEvent(BeaconCompanionEvent.SelectBeaconMode(it)) }
                )
            }

            // CMF Watch Dial Visualizer
            item(key = "cmf_watch") {
                CmfWatchDialVisualizer(
                    metrics = state.watchMetrics,
                    zone = state.session.heartRateZone,
                    onConnectClick = { onEvent(BeaconCompanionEvent.ConnectCmfWatch) },
                    onTriggerWristGesture = { onEvent(BeaconCompanionEvent.SimulateWatchGesture) }
                )
            }

            // Nothing Phone Glyph Visualizer
            item(key = "glyph_visualizer") {
                GlyphBeaconVisualizer(
                    session = state.session,
                    deviceModel = state.deviceModel
                )
            }

            // Smart Bike Speed & Brake Card
            item(key = "bike_telemetry") {
                BrakeSpeedCard(
                    speedKmh = state.session.speedKmh,
                    isBraking = state.session.isBrakingDecelerating,
                    onSimulateSpeedChange = { speed, brake ->
                        onEvent(BeaconCompanionEvent.SimulateSpeedAndBrake(speed, brake))
                    }
                )
            }

            // Optical Morse Code SOS Card
            item(key = "morse_studio") {
                MorseStudioCard(
                    isTransmitting = state.session.isMorseTransmitting,
                    currentText = state.session.morseTransmissionText,
                    onTransmitSos = { onEvent(BeaconCompanionEvent.TransmitSos) },
                    onTransmitCustomText = { onEvent(BeaconCompanionEvent.TransmitCustomMorse(it)) },
                    onCancelTransmission = { onEvent(BeaconCompanionEvent.CancelMorse) }
                )
            }

            // Acoustic / Watch Finder Card
            item(key = "acoustic_finder") {
                AcousticFinderCard(
                    isListening = state.session.isClapDetectorListening,
                    onToggleClap = { onEvent(BeaconCompanionEvent.ToggleClapDetector(it)) },
                    onTriggerStrobe = { onEvent(BeaconCompanionEvent.TriggerFindPhoneStrobe) }
                )
            }

            // Footer
            item(key = "footer") {
                Text(
                    text = "GLYPH BEACON & CMF WATCH PRO 2 COMPANION\nBluetooth LE • Motion Fusion • GDK Integration",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NothingWhiteMuted.copy(alpha = 0.5f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeSelectorGrid(
    activeMode: GlyphBeaconMode,
    onSelectMode: (GlyphBeaconMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NothingDarkSurface)
            .border(1.dp, NothingCardBorder, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CmfOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SELECT ACTIVE BEACON MODE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite,
                    letterSpacing = 1.sp
                )
            }

            GlyphBeaconMode.entries.filter { it != GlyphBeaconMode.OFF }.forEach { mode ->
                val isSelected = mode == activeMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NothingCardBorder else Color(0xFF161616))
                        .border(
                            1.dp,
                            if (isSelected) CmfOrange.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CmfOrange else NothingWhite
                        )
                        Text(
                            text = mode.subtitle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = NothingWhiteMuted
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CmfOrange)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AcousticFinderCard(
    isListening: Boolean,
    onToggleClap: (Boolean) -> Unit,
    onTriggerStrobe: () -> Unit,
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
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "  ACOUSTIC CLAP & WATCH FINDER",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Double-Clap Detection",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NothingWhite
                    )
                    Text(
                        text = "Clap twice to make phone strobe in dark room",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingWhiteMuted
                    )
                }
                Switch(
                    checked = isListening,
                    onCheckedChange = onToggleClap,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = CmfOrange,
                        uncheckedThumbColor = NothingWhiteMuted,
                        uncheckedTrackColor = NothingBlack
                    )
                )
            }

            Button(
                onClick = onTriggerStrobe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingWhite,
                    contentColor = NothingBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Highlight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "  TRIGGER ULTRA-BRIGHT FIND STROBE (12x)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
