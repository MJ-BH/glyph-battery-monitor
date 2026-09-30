package com.nothing.glyphbattery.presentation.ui.dashboard

import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel

data class BeaconCompanionState(
    val session: BeaconSession = BeaconSession(),
    val watchMetrics: CmfWatchMetrics = CmfWatchMetrics(),
    val deviceModel: NothingDeviceModel = NothingDeviceModel.PHONE_2A,
    val feedbackMessage: String? = null
)

sealed interface BeaconCompanionEvent {
    data class SelectBeaconMode(val mode: GlyphBeaconMode) : BeaconCompanionEvent
    data class SelectDeviceModel(val model: NothingDeviceModel) : BeaconCompanionEvent
    data class SimulateSpeedAndBrake(val speedKmh: Float, val isBraking: Boolean) : BeaconCompanionEvent
    data object TransmitSos : BeaconCompanionEvent
    data class TransmitCustomMorse(val text: String) : BeaconCompanionEvent
    data object CancelMorse : BeaconCompanionEvent
    data object TriggerFindPhoneStrobe : BeaconCompanionEvent
    data class ToggleClapDetector(val enabled: Boolean) : BeaconCompanionEvent
    data object ConnectCmfWatch : BeaconCompanionEvent
    data class SetSimulatedHeartRate(val bpm: Int) : BeaconCompanionEvent
    data object SimulateWatchGesture : BeaconCompanionEvent
    data object ClearFeedback : BeaconCompanionEvent
}
