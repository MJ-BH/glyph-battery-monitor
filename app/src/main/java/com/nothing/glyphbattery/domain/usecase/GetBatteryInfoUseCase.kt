package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.core.result.Result
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.repository.BatteryRepository

class GetBatteryInfoUseCase(
    private val batteryRepository: BatteryRepository
) {
    suspend operator fun invoke(): Result<BatteryInfo, Throwable> {
        return batteryRepository.getBatteryInfoSnapshot()
    }
}
