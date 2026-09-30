package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.repository.BatteryRepository

/**
 * UseCase to get an immediate snapshot of current battery metrics.
 */
class GetBatteryInfoUseCase(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): BatteryInfo {
        return batteryRepository.getBatteryInfoSnapshot()
    }
}
