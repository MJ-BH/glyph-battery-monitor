package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.repository.BeaconRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveBeaconMetricsUseCase(
    private val beaconRepository: BeaconRepository
) {
    operator fun invoke(): StateFlow<BeaconSession> = beaconRepository.sessionState
}
