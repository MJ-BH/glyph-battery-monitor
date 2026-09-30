package com.nothing.glyphbattery.data.battery

import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow

class BatteryRepositoryImpl(
    private val batteryDataSource: BatteryDataSource
) : BatteryRepository {

    override fun observeBatteryInfo(): Flow<BatteryInfo> {
        return batteryDataSource.observeBattery()
    }

    override fun getBatteryInfoSnapshot(): BatteryInfo {
        return batteryDataSource.getImmediateSnapshot()
    }
}
