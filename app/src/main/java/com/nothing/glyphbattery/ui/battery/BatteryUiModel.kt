package com.nothing.glyphbattery.ui.battery

import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel

data class BatteryUiModel(
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val glyphState: GlyphState = GlyphState(),
    val isFlashingNow: Boolean = false,
    val feedbackMessage: String? = null
)

sealed interface BatteryUiEvent {
    data object FlashGlyphBattery : BatteryUiEvent
    data class ChangeAnimationMode(val mode: GlyphAnimationMode) : BatteryUiEvent
    data class ToggleChargingGlow(val enabled: Boolean) : BatteryUiEvent
    data class ToggleFlashOnPlugIn(val enabled: Boolean) : BatteryUiEvent
    data class SelectDeviceModel(val model: NothingDeviceModel) : BatteryUiEvent
    data class SetCustomProgress(val progress: Int) : BatteryUiEvent
    data object ClearFeedbackMessage : BatteryUiEvent
}
