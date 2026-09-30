package com.nothing.glyphbattery.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerGlyphBatteryFlashUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BatteryDashboardViewModel(
    private val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase,
    private val controlGlyphUseCase: ControlGlyphUseCase,
    private val triggerGlyphBatteryFlashUseCase: TriggerGlyphBatteryFlashUseCase,
    private val glyphRepository: GlyphRepository
) : ViewModel() {

    private val _uiStateInternal = MutableStateFlow(BatteryDashboardState())

    val uiState: StateFlow<BatteryDashboardState> = combine(
        _uiStateInternal,
        observeBatteryInfoUseCase(),
        glyphRepository.glyphState
    ) { currentUiState, batteryInfo, glyphState ->
        currentUiState.copy(
            batteryInfo = batteryInfo,
            glyphState = glyphState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryDashboardState()
    )

    init {
        viewModelScope.launch {
            glyphRepository.initialize()
            // Auto sync progress with current battery
            val currentInfo = uiState.value.batteryInfo
            if (currentInfo.level > 0) {
                glyphRepository.displayProgress(currentInfo.level)
            }
        }
    }

    fun onEvent(event: BatteryDashboardEvent) {
        when (event) {
            BatteryDashboardEvent.FlashGlyphBattery -> {
                flashBatteryGlyph()
            }
            is BatteryDashboardEvent.ChangeAnimationMode -> {
                viewModelScope.launch {
                    controlGlyphUseCase.setAnimationMode(event.mode)
                    _uiStateInternal.update {
                        it.copy(feedbackMessage = "Glyph Mode set to: ${event.mode.title}")
                    }
                }
            }
            is BatteryDashboardEvent.ToggleChargingGlow -> {
                viewModelScope.launch {
                    controlGlyphUseCase.setSyncWithCharging(event.enabled)
                    _uiStateInternal.update {
                        it.copy(feedbackMessage = if (event.enabled) "Glyph Charging Glow Enabled" else "Glyph Charging Glow Disabled")
                    }
                }
            }
            is BatteryDashboardEvent.ToggleFlashOnPlugIn -> {
                _uiStateInternal.update {
                    it.copy(
                        glyphState = it.glyphState.copy(flashOnPlugIn = event.enabled),
                        feedbackMessage = if (event.enabled) "Flash on plug-in Enabled" else "Flash on plug-in Disabled"
                    )
                }
            }
            is BatteryDashboardEvent.SelectDeviceModel -> {
                viewModelScope.launch {
                    controlGlyphUseCase.selectDeviceModel(event.model)
                    _uiStateInternal.update {
                        it.copy(feedbackMessage = "Switched model to ${event.model.displayName}")
                    }
                }
            }
            is BatteryDashboardEvent.SetCustomProgressPreview -> {
                viewModelScope.launch {
                    controlGlyphUseCase.updateBatteryProgress(event.progress)
                }
            }
            BatteryDashboardEvent.ClearFeedbackMessage -> {
                _uiStateInternal.update { it.copy(feedbackMessage = null) }
            }
        }
    }

    private fun flashBatteryGlyph() {
        viewModelScope.launch {
            _uiStateInternal.update { it.copy(isFlashingNow = true) }
            val level = uiState.value.batteryInfo.level
            triggerGlyphBatteryFlashUseCase(level)
            _uiStateInternal.update {
                it.copy(feedbackMessage = "Flashed $level% on Glyph LEDs")
            }
            delay(2500)
            _uiStateInternal.update { it.copy(isFlashingNow = false) }
        }
    }
}
