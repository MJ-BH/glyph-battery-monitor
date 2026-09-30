package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.repository.BeaconRepository

class ControlGlyphBeaconUseCase(
    private val beaconRepository: BeaconRepository
) {
    suspend fun setMode(mode: GlyphBeaconMode) {
        beaconRepository.setBeaconMode(mode)
    }

    suspend fun stopBeacon() {
        beaconRepository.stopBeacon()
    }

    fun setClapDetectorEnabled(enabled: Boolean) {
        beaconRepository.setClapDetectorEnabled(enabled)
    }

    fun setSimulatedSpeedAndBrake(speedKmh: Float, isBraking: Boolean) {
        beaconRepository.updateSimulatedSpeed(speedKmh, isBraking)
    }
}
