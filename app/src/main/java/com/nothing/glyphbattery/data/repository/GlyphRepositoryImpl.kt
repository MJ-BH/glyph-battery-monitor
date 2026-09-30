package com.nothing.glyphbattery.data.repository

import com.nothing.glyphbattery.data.mapper.GlyphMapper
import com.nothing.glyphbattery.data.source.GlyphManagerBridge
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GlyphRepositoryImpl(
    private val bridge: GlyphManagerBridge,
    private val mapper: GlyphMapper,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GlyphRepository {

    override val glyphState: StateFlow<GlyphState> = bridge.glyphDtoState
        .map { dto -> mapper.mapToDomain(dto) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = mapper.mapToDomain(bridge.glyphDtoState.value)
        )

    private var animationJob: Job? = null

    override suspend fun initialize() = withContext(ioDispatcher) {
        bridge.init()
    }

    override suspend fun openSession(): Boolean = withContext(ioDispatcher) {
        bridge.openSession()
    }

    override suspend fun closeSession() = withContext(ioDispatcher) {
        animationJob?.cancel()
        bridge.closeSession()
    }

    override suspend fun displayProgress(progress: Int, reverse: Boolean) = withContext(ioDispatcher) {
        bridge.displayProgress(progress, reverse)
    }

    override suspend fun setAnimationMode(mode: GlyphAnimationMode) = withContext(ioDispatcher) {
        animationJob?.cancel()
        when (mode) {
            GlyphAnimationMode.PROGRESS_BAR -> {
                bridge.displayProgress(glyphState.value.currentProgress)
            }
            GlyphAnimationMode.BREATHING_CHARGING -> {
                animationJob = scope.launch {
                    while (isActive) {
                        bridge.animateBreathing()
                        delay(1800)
                    }
                }
            }
            GlyphAnimationMode.PULSE_ALERT -> {
                bridge.triggerQuickBatteryPulse(glyphState.value.currentProgress)
            }
            GlyphAnimationMode.STEADY_ON -> {
                bridge.displayProgress(100)
            }
            GlyphAnimationMode.OFF -> {
                bridge.turnOff()
            }
        }
    }

    override suspend fun triggerBatteryFlash(batteryLevel: Int) = withContext(ioDispatcher) {
        bridge.triggerQuickBatteryPulse(batteryLevel)
    }

    override suspend fun setSyncWithCharging(enabled: Boolean) = withContext(ioDispatcher) {
        bridge.setSyncCharging(enabled)
    }

    override suspend fun setDeviceModel(model: NothingDeviceModel) = withContext(ioDispatcher) {
        bridge.setModel(model)
    }

    override fun release() {
        animationJob?.cancel()
        bridge.closeSession()
    }
}
