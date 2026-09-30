package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.repository.BeaconRepository

class TriggerFindPhoneStrobeUseCase(
    private val beaconRepository: BeaconRepository
) {
    suspend operator fun invoke() {
        beaconRepository.triggerEmergencyFindStrobe()
    }
}
