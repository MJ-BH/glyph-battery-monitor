package com.nothing.glyphbattery.data.glyph

import android.content.Context
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository
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

class GlyphRepositoryImpl(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : GlyphRepository {

    private val bridge = GlyphManagerBridge(context, scope)
    private val _glyphState = MutableStateFlow(GlyphState())
    override val glyphState: StateFlow<GlyphState> = _glyphState.asStateFlow()

    private var animationJob: Job? = null

    init {
        scope.launch {
            bridge.connectedFlow.collect { connected ->
                _glyphState.update { it.copy(isConnected = connected) }
            }
        }
        scope.launch {
            bridge.activeChannelsFlow.collect { channels ->
                _glyphState.update { it.copy(activeChannels = channels) }
            }
        }
        scope.launch {
            bridge.detectedModel.collect { model ->
                _glyphState.update { it.copy(deviceModel = model) }
            }
        }
    }

    override suspend fun initialize() {
        bridge.init()
    }

    override suspend fun openSession(): Boolean {
        val success = bridge.openSession()
        _glyphState.update { it.copy(isSessionOpen = success) }
        return success
    }

    override suspend fun closeSession() {
        animationJob?.cancel()
        bridge.closeSession()
        _glyphState.update { it.copy(isSessionOpen = false, activeChannels = emptySet()) }
    }

    override suspend fun displayProgress(progress: Int, reverse: Boolean) {
        _glyphState.update { it.copy(currentProgress = progress) }
        bridge.displayProgress(progress, reverse)
    }

    override suspend fun setAnimationMode(mode: GlyphAnimationMode) {
        _glyphState.update { it.copy(activeMode = mode) }
        animationJob?.cancel()

        when (mode) {
            GlyphAnimationMode.PROGRESS_BAR -> {
                displayProgress(_glyphState.value.currentProgress)
            }
            GlyphAnimationMode.BREATHING_CHARGING -> {
                startBreathingAnimation()
            }
            GlyphAnimationMode.PULSE_ALERT -> {
                bridge.triggerQuickBatteryPulse(_glyphState.value.currentProgress)
            }
            GlyphAnimationMode.STEADY_ON -> {
                displayProgress(100)
            }
            GlyphAnimationMode.OFF -> {
                bridge.turnOff()
            }
        }
    }

    private fun startBreathingAnimation() {
        animationJob = scope.launch {
            while (isActive) {
                bridge.animateBreathingCycle()
                delay(2000)
            }
        }
    }

    override suspend fun triggerBatteryFlash(batteryLevel: Int) {
        _glyphState.update {
            it.copy(
                currentProgress = batteryLevel,
                lastFlashedTimestamp = System.currentTimeMillis()
            )
        }
        bridge.triggerQuickBatteryPulse(batteryLevel)
    }

    override suspend fun setSyncWithCharging(enabled: Boolean) {
        _glyphState.update { it.copy(syncWithCharging = enabled) }
    }

    override suspend fun setDeviceModel(model: NothingDeviceModel) {
        bridge.setManualModel(model)
        _glyphState.update { it.copy(deviceModel = model) }
    }

    override fun release() {
        animationJob?.cancel()
        bridge.closeSession()
    }
}
