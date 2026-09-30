package com.nothing.glyphbattery.data.beacon

import android.content.Context
import android.util.Log
import com.nothing.glyphbattery.data.audio.ClapDetectorSource
import com.nothing.glyphbattery.data.ble.CmfWatchRepositoryImpl
import com.nothing.glyphbattery.data.glyph.GlyphManagerBridge
import com.nothing.glyphbattery.data.motion.SpeedAndBrakeDetector
import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.model.HeartRateZone
import com.nothing.glyphbattery.domain.model.MorseMessage
import com.nothing.glyphbattery.domain.repository.BeaconRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BeaconRepositoryImpl(
    private val context: Context,
    private val cmfWatchRepository: CmfWatchRepositoryImpl,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : BeaconRepository {

    private val tag = "BeaconRepositoryImpl"

    private val glyphBridge = GlyphManagerBridge(context, scope)
    private val motionDetector = SpeedAndBrakeDetector(context)
    private val clapDetector = ClapDetectorSource(context, scope)

    private val _sessionState = MutableStateFlow(BeaconSession())
    override val sessionState: StateFlow<BeaconSession> = _sessionState.asStateFlow()

    private var activeStrobeJob: Job? = null
    private var morseJob: Job? = null

    init {
        glyphBridge.init()
        motionDetector.startListening()

        // Sync speed & brake state
        scope.launch {
            motionDetector.motionData.collect { motion ->
                _sessionState.update {
                    it.copy(
                        speedKmh = motion.speedKmh,
                        isBrakingDecelerating = motion.isBraking
                    )
                }
                handleMotionUpdate(motion.speedKmh, motion.isBraking)
            }
        }

        // Sync CMF Watch Heart Rate
        scope.launch {
            cmfWatchRepository.watchMetrics.collect { metrics ->
                val zone = HeartRateZone.fromBpm(metrics.heartRateBpm)
                _sessionState.update {
                    it.copy(
                        currentBpm = metrics.heartRateBpm,
                        heartRateZone = zone
                    )
                }
                if (_sessionState.value.activeMode == GlyphBeaconMode.HEART_BIO_PULSE) {
                    restartHeartBioPulse(zone)
                }
            }
        }

        // Listen for Acoustic Claps
        scope.launch {
            clapDetector.clapTriggeredTimestamp.collect { ts ->
                if (ts > 0) {
                    _sessionState.update { it.copy(lastClapTriggerTimestamp = ts) }
                    triggerEmergencyFindStrobe()
                }
            }
        }
    }

    override suspend fun setBeaconMode(mode: GlyphBeaconMode) {
        _sessionState.update { it.copy(activeMode = mode) }
        activeStrobeJob?.cancel()
        morseJob?.cancel()

        glyphBridge.openSession()

        when (mode) {
            GlyphBeaconMode.BIKE_SMART_STROBE -> {
                startSmartBikeBeacon()
            }
            GlyphBeaconMode.HEART_BIO_PULSE -> {
                cmfWatchRepository.startScanningAndConnect()
                restartHeartBioPulse(_sessionState.value.heartRateZone)
            }
            GlyphBeaconMode.MORSE_OPTICAL_SOS -> {
                transmitMorseMessage("SOS")
            }
            GlyphBeaconMode.CLAP_DARK_FINDER -> {
                setClapDetectorEnabled(true)
            }
            GlyphBeaconMode.STEADY_REAR_LIGHT -> {
                glyphBridge.displayProgress(100)
            }
            GlyphBeaconMode.OFF -> {
                stopBeacon()
            }
        }
    }

    override suspend fun startSmartBikeBeacon() {
        activeStrobeJob?.cancel()
        activeStrobeJob = scope.launch {
            while (isActive) {
                val isBraking = _sessionState.value.isBrakingDecelerating
                val speed = _sessionState.value.speedKmh

                if (isBraking) {
                    // Solid Full Brightness Brake Light
                    glyphBridge.displayProgress(100)
                    _sessionState.update { it.copy(isStrobingActive = true, currentStrobePulseAlpha = 1.0f) }
                    delay(300)
                } else {
                    // Speed-adaptive flash: Faster flashing when riding faster
                    val flashIntervalMs = when {
                        speed > 25f -> 180L
                        speed > 15f -> 300L
                        else -> 500L
                    }

                    glyphBridge.displayProgress(100)
                    _sessionState.update { it.copy(isStrobingActive = true, currentStrobePulseAlpha = 1.0f) }
                    delay(80)

                    glyphBridge.displayProgress(0)
                    _sessionState.update { it.copy(isStrobingActive = false, currentStrobePulseAlpha = 0.0f) }
                    delay(flashIntervalMs)
                }
            }
        }
    }

    private fun restartHeartBioPulse(zone: HeartRateZone) {
        activeStrobeJob?.cancel()
        activeStrobeJob = scope.launch {
            while (isActive) {
                // Heart systolic 'lub-dub' double pulse
                glyphBridge.displayProgress(80)
                _sessionState.update { it.copy(isStrobingActive = true, currentStrobePulseAlpha = 1.0f) }
                delay(90)

                glyphBridge.displayProgress(0)
                _sessionState.update { it.copy(isStrobingActive = false, currentStrobePulseAlpha = 0.0f) }
                delay(70)

                glyphBridge.displayProgress(100)
                _sessionState.update { it.copy(isStrobingActive = true, currentStrobePulseAlpha = 1.0f) }
                delay(120)

                glyphBridge.displayProgress(0)
                _sessionState.update { it.copy(isStrobingActive = false, currentStrobePulseAlpha = 0.0f) }
                delay((zone.pulseCadenceMs - 280).coerceAtLeast(150L))
            }
        }
    }

    override suspend fun transmitMorseMessage(text: String) {
        morseJob?.cancel()
        _sessionState.update {
            it.copy(
                morseTransmissionText = text,
                isMorseTransmitting = true,
                activeMode = GlyphBeaconMode.MORSE_OPTICAL_SOS
            )
        }

        morseJob = scope.launch {
            val sequence = MorseMessage.toSignalSequence(text)
            while (isActive && _sessionState.value.isMorseTransmitting) {
                for (signal in sequence) {
                    when (signal) {
                        is MorseMessage.MorseSignal.LightOn -> {
                            glyphBridge.displayProgress(100)
                            _sessionState.update { it.copy(currentStrobePulseAlpha = 1.0f) }
                            delay(signal.durationMs)
                        }
                        is MorseMessage.MorseSignal.LightOff -> {
                            glyphBridge.displayProgress(0)
                            _sessionState.update { it.copy(currentStrobePulseAlpha = 0.0f) }
                            delay(signal.durationMs)
                        }
                    }
                }
                delay(2000) // 2-second repeat interval for SOS
            }
        }
    }

    override suspend fun cancelMorseTransmission() {
        morseJob?.cancel()
        glyphBridge.displayProgress(0)
        _sessionState.update { it.copy(isMorseTransmitting = false, morseTransmissionText = null) }
    }

    override suspend fun triggerEmergencyFindStrobe() {
        activeStrobeJob?.cancel()
        activeStrobeJob = scope.launch {
            repeat(12) {
                glyphBridge.displayProgress(100)
                _sessionState.update { it.copy(isStrobingActive = true, currentStrobePulseAlpha = 1.0f) }
                delay(60)

                glyphBridge.displayProgress(0)
                _sessionState.update { it.copy(isStrobingActive = false, currentStrobePulseAlpha = 0.0f) }
                delay(60)
            }
        }
    }

    override fun setClapDetectorEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isClapDetectorListening = enabled) }
        if (enabled) {
            clapDetector.startListening()
        } else {
            clapDetector.stopListening()
        }
    }

    override fun updateSimulatedSpeed(speedKmh: Float, isBraking: Boolean) {
        motionDetector.updateManualSpeedAndBrake(speedKmh, isBraking)
    }

    private fun handleMotionUpdate(speedKmh: Float, isBraking: Boolean) {
        if (_sessionState.value.activeMode == GlyphBeaconMode.BIKE_SMART_STROBE) {
            // Speed change automatically picked up by smart bike coroutine
        }
    }

    override suspend fun stopBeacon() {
        activeStrobeJob?.cancel()
        morseJob?.cancel()
        clapDetector.stopListening()
        glyphBridge.closeSession()
        _sessionState.update {
            it.copy(
                isStrobingActive = false,
                isMorseTransmitting = false,
                isClapDetectorListening = false,
                currentStrobePulseAlpha = 0.0f,
                activeMode = GlyphBeaconMode.OFF
            )
        }
    }
}
