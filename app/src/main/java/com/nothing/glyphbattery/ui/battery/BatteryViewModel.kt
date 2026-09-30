package com.nothing.glyphbattery.ui.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerGlyphBatteryFlashUseCase
import com.nothing.glyphbattery.ui.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BatteryViewModel(
    private val observeBatteryInfoUseCase: ObserveBatteryInfoUseCase,
    private val controlGlyphUseCase: ControlGlyphUseCase,
    private val triggerGlyphBatteryFlashUseCase: TriggerGlyphBatteryFlashUseCase,
    private val glyphRepository: GlyphRepository
) : ViewModel() {

    private val _internalUiModel = MutableStateFlow(BatteryUiModel())

    val uiState: StateFlow<UiState<BatteryUiModel>> = combine(
        _internalUiModel,
        observeBatteryInfoUseCase(),
        glyphRepository.glyphState
    ) { currentModel, batteryInfo, glyphState ->
        val updated = currentModel.copy(
            batteryInfo = batteryInfo,
            glyphState = glyphState
        )
        UiState.Success(updated) as UiState<BatteryUiModel>
    }.catch { error ->
        emit(UiState.Error(error.message ?: "Failed to read battery state", error))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch {
            glyphRepository.initialize()
            // Auto sync progress with current battery
            val currentLevel = (uiState.value as? UiState.Success)?.data?.batteryInfo?.level ?: 50
            if (currentLevel > 0) {
                glyphRepository.displayProgress(currentLevel)
            }
        }

        viewModelScope.launch {
            var wasCharging: Boolean? = null
            observeBatteryInfoUseCase().collect { info ->
                if (wasCharging != null && wasCharging == false && info.isCharging) {
                    // Transitioned from unplugged to plugged in
                    flashBatteryOnGlyph()
                }
                wasCharging = info.isCharging
            }
        }

        viewModelScope.launch {
            var previousFaceDown = false
            combine(
                observeBatteryInfoUseCase(),
                glyphRepository.glyphState
            ) { battery, glyph ->
                Pair(battery, glyph)
            }.collect { (battery, glyph) ->
                if (glyph.flipToGlyphCharging && glyph.isFaceDown && battery.isCharging) {
                    // Continuous progress update while turned face-down and charging
                    glyphRepository.openSession()
                    glyphRepository.displayProgress(battery.level)
                    previousFaceDown = true
                } else if (previousFaceDown && (!glyph.isFaceDown || !battery.isCharging)) {
                    // Device picked up or unplugged
                    glyphRepository.turnOff()
                    previousFaceDown = false
                }
            }
        }
    }

    fun onEvent(event: BatteryUiEvent) {
        when (event) {
            BatteryUiEvent.FlashGlyphBattery -> {
                flashBatteryOnGlyph()
            }
            is BatteryUiEvent.ChangeAnimationMode -> {
                viewModelScope.launch {
                    controlGlyphUseCase.setAnimationMode(event.mode)
                    _internalUiModel.update {
                        it.copy(feedbackMessage = "Glyph Mode set to: ${event.mode.title}")
                    }
                }
            }
            is BatteryUiEvent.ToggleChargingGlow -> {
                viewModelScope.launch {
                    controlGlyphUseCase.setSyncWithCharging(event.enabled)
                    _internalUiModel.update {
                        it.copy(feedbackMessage = if (event.enabled) "Glyph Charging Glow Enabled" else "Glyph Charging Glow Disabled")
                    }
                }
            }
            is BatteryUiEvent.ToggleFlashOnPlugIn -> {
                _internalUiModel.update {
                    it.copy(
                        glyphState = it.glyphState.copy(flashOnPlugIn = event.enabled),
                        feedbackMessage = if (event.enabled) "Flash on plug-in Enabled" else "Flash on plug-in Disabled"
                    )
                }
            }
            is BatteryUiEvent.ToggleFlipToGlyph -> {
                viewModelScope.launch {
                    controlGlyphUseCase.setFlipToGlyphCharging(event.enabled)
                    _internalUiModel.update {
                        it.copy(feedbackMessage = if (event.enabled) "Flip-to-Glyph Charging Enabled" else "Flip-to-Glyph Charging Disabled")
                    }
                }
            }
            is BatteryUiEvent.SelectDeviceModel -> {
                viewModelScope.launch {
                    controlGlyphUseCase.selectDeviceModel(event.model)
                    _internalUiModel.update {
                        it.copy(feedbackMessage = "Switched to ${event.model.displayName}")
                    }
                }
            }
            is BatteryUiEvent.SetCustomProgress -> {
                viewModelScope.launch {
                    controlGlyphUseCase.updateBatteryProgress(event.progress)
                }
            }
            BatteryUiEvent.ClearFeedbackMessage -> {
                _internalUiModel.update { it.copy(feedbackMessage = null) }
            }
        }
    }

    private fun flashBatteryOnGlyph() {
        viewModelScope.launch {
            _internalUiModel.update { it.copy(isFlashingNow = true) }
            val currentLevel = (uiState.value as? UiState.Success)?.data?.batteryInfo?.level ?: 50
            triggerGlyphBatteryFlashUseCase(currentLevel)
            _internalUiModel.update {
                it.copy(feedbackMessage = "Flashed $currentLevel% on Glyph LEDs")
            }
            delay(2500)
            _internalUiModel.update { it.copy(isFlashingNow = false) }
        }
    }
}
