package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.repository.BeaconRepository

class TransmitMorseSosUseCase(
    private val beaconRepository: BeaconRepository
) {
    suspend fun transmitSos() {
        beaconRepository.transmitMorseMessage("SOS")
    }

    suspend fun transmitCustomMessage(text: String) {
        beaconRepository.transmitMorseMessage(text)
    }

    suspend fun cancel() {
        beaconRepository.cancelMorseTransmission()
    }
}
