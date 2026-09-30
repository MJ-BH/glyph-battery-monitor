package com.nothing.glyphbattery.domain.repository

import com.nothing.glyphbattery.core.result.Result
import com.nothing.glyphbattery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun observeBatteryInfo(): Flow<BatteryInfo>
    suspend fun getBatteryInfoSnapshot(): Result<BatteryInfo, Throwable>
    suspend fun getBatteryInfo(): Result<BatteryInfo, Throwable> = getBatteryInfoSnapshot()
}
