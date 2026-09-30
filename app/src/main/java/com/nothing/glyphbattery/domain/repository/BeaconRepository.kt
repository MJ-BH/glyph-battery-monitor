package com.nothing.glyphbattery.domain.repository

import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import kotlinx.coroutines.flow.StateFlow

interface BeaconRepository {
    val sessionState: StateFlow<BeaconSession>

    suspend fun setBeaconMode(mode: GlyphBeaconMode)
    suspend fun startSmartBikeBeacon()
    suspend fun stopBeacon()
    suspend fun transmitMorseMessage(text: String)
    suspend fun cancelMorseTransmission()
    suspend fun triggerEmergencyFindStrobe()
    fun setClapDetectorEnabled(enabled: Boolean)
    fun updateSimulatedSpeed(speedKmh: Float, isBraking: Boolean)
}
