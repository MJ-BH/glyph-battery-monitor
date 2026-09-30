package com.nothing.glyphbattery.data.repository

import com.nothing.glyphbattery.core.result.Result
import com.nothing.glyphbattery.data.mapper.BatteryMapper
import com.nothing.glyphbattery.data.source.BatteryDataSource
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BatteryRepositoryImpl(
    private val batteryDataSource: BatteryDataSource,
    private val batteryMapper: BatteryMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BatteryRepository {

    override fun observeBatteryInfo(): Flow<BatteryInfo> {
        return batteryDataSource.observeBatteryDto()
            .map { dto -> batteryMapper.mapToDomain(dto) }
            .flowOn(ioDispatcher)
    }

    override suspend fun getBatteryInfoSnapshot(): Result<BatteryInfo, Throwable> {
        return withContext(ioDispatcher) {
            try {
                val dto = batteryDataSource.getImmediateSnapshotDto()
                val domain = batteryMapper.mapToDomain(dto)
                Result.Success(domain)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }
    }
}
