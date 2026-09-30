package com.nothing.glyphbattery.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.usecase.ControlGlyphBeaconUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBeaconMetricsUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveCmfHeartRateUseCase
import com.nothing.glyphbattery.domain.usecase.TransmitMorseSosUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerFindPhoneStrobeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BeaconCompanionViewModel(
    private val observeBeaconMetricsUseCase: ObserveBeaconMetricsUseCase,
    private val observeCmfHeartRateUseCase: ObserveCmfHeartRateUseCase,
    private val controlGlyphBeaconUseCase: ControlGlyphBeaconUseCase,
    private val transmitMorseSosUseCase: TransmitMorseSosUseCase,
    private val triggerFindPhoneStrobeUseCase: TriggerFindPhoneStrobeUseCase
) : ViewModel() {

    private val _uiStateInternal = MutableStateFlow(BeaconCompanionState())

    val uiState: StateFlow<BeaconCompanionState> = combine(
        _uiStateInternal,
        observeBeaconMetricsUseCase(),
        observeCmfHeartRateUseCase()
    ) { currentUiState, session, watchMetrics ->
        currentUiState.copy(
            session = session,
            watchMetrics = watchMetrics
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BeaconCompanionState()
    )

    init {
        viewModelScope.launch {
            // Start listening to CMF Watch Pro 2
            observeCmfHeartRateUseCase.startScanning()
            // Default to Smart Bike Beacon
            controlGlyphBeaconUseCase.setMode(GlyphBeaconMode.BIKE_SMART_STROBE)
        }
    }

    fun onEvent(event: BeaconCompanionEvent) {
        when (event) {
            is BeaconCompanionEvent.SelectBeaconMode -> {
                viewModelScope.launch {
                    controlGlyphBeaconUseCase.setMode(event.mode)
                    _uiStateInternal.update {
                        it.copy(feedbackMessage = "Active Mode: ${event.mode.title}")
                    }
                }
            }
            is BeaconCompanionEvent.SelectDeviceModel -> {
                _uiStateInternal.update {
                    it.copy(
                        deviceModel = event.model,
                        feedbackMessage = "Switched to ${event.model.displayName}"
                    )
                }
            }
            is BeaconCompanionEvent.SimulateSpeedAndBrake -> {
                controlGlyphBeaconUseCase.setSimulatedSpeedAndBrake(event.speedKmh, event.isBraking)
            }
            BeaconCompanionEvent.TransmitSos -> {
                viewModelScope.launch {
                    transmitMorseSosUseCase.transmitSos()
                    _uiStateInternal.update { it.copy(feedbackMessage = "Transmitting Emergency SOS (... --- ...)") }
                }
            }
            is BeaconCompanionEvent.TransmitCustomMorse -> {
                viewModelScope.launch {
                    transmitMorseSosUseCase.transmitCustomMessage(event.text)
                    _uiStateInternal.update { it.copy(feedbackMessage = "Transmitting Morse: ${event.text}") }
                }
            }
            BeaconCompanionEvent.CancelMorse -> {
                viewModelScope.launch {
                    transmitMorseSosUseCase.cancel()
                    _uiStateInternal.update { it.copy(feedbackMessage = "Morse Transmission Cancelled") }
                }
            }
            BeaconCompanionEvent.TriggerFindPhoneStrobe -> {
                viewModelScope.launch {
                    triggerFindPhoneStrobeUseCase()
                    _uiStateInternal.update { it.copy(feedbackMessage = "High-Intensity Strobe Triggered!") }
                }
            }
            is BeaconCompanionEvent.ToggleClapDetector -> {
                controlGlyphBeaconUseCase.setClapDetectorEnabled(event.enabled)
                _uiStateInternal.update {
                    it.copy(feedbackMessage = if (event.enabled) "Clap Detector Listening" else "Clap Detector Off")
                }
            }
            BeaconCompanionEvent.ConnectCmfWatch -> {
                observeCmfHeartRateUseCase.startScanning()
                _uiStateInternal.update { it.copy(feedbackMessage = "Scanning for CMF Watch Pro 2...") }
            }
            is BeaconCompanionEvent.SetSimulatedHeartRate -> {
                observeCmfHeartRateUseCase.setSimulatedHeartRate(event.bpm)
            }
            BeaconCompanionEvent.SimulateWatchGesture -> {
                observeCmfHeartRateUseCase.triggerWristGesture()
                viewModelScope.launch {
                    triggerFindPhoneStrobeUseCase()
                    _uiStateInternal.update { it.copy(feedbackMessage = "CMF Watch Remote Trigger Received!") }
                }
            }
            BeaconCompanionEvent.ClearFeedback -> {
                _uiStateInternal.update { it.copy(feedbackMessage = null) }
            }
        }
    }
}
