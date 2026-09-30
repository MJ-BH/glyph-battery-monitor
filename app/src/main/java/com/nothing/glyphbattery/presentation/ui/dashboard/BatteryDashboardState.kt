package com.nothing.glyphbattery.presentation.ui.dashboard

import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel

data class BatteryDashboardState(
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val glyphState: GlyphState = GlyphState(),
    val isForegroundServiceRunning: Boolean = false,
    val isFlashingNow: Boolean = false,
    val feedbackMessage: String? = null
)

sealed interface BatteryDashboardEvent {
    data object FlashGlyphBattery : BatteryDashboardEvent
    data class ChangeAnimationMode(val mode: GlyphAnimationMode) : BatteryDashboardEvent
    data class ToggleChargingGlow(val enabled: Boolean) : BatteryDashboardEvent
    data class ToggleFlashOnPlugIn(val enabled: Boolean) : BatteryDashboardEvent
    data class SelectDeviceModel(val model: NothingDeviceModel) : BatteryDashboardEvent
    data class SetCustomProgressPreview(val progress: Int) : BatteryDashboardEvent
    data object ClearFeedbackMessage : BatteryDashboardEvent
}
