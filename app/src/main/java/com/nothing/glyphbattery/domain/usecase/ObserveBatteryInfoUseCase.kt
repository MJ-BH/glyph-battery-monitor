package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to continuously observe device battery updates.
 */
class ObserveBatteryInfoUseCase(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<BatteryInfo> {
        return batteryRepository.observeBatteryInfo()
    }
}
