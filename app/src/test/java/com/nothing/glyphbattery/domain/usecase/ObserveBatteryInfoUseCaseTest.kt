package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.core.result.Result
import com.nothing.glyphbattery.domain.model.BatteryHealth
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.PluggedType
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveBatteryInfoUseCaseTest {

    private class FakeBatteryRepository(
        private val mockInfo: BatteryInfo
    ) : BatteryRepository {
        override fun observeBatteryInfo(): Flow<BatteryInfo> = flowOf(mockInfo)
        override suspend fun getBatteryInfoSnapshot(): Result<BatteryInfo, Throwable> = Result.Success(mockInfo)
    }

    @Test
    fun `usecase emits battery information from repository accurately`() = runTest {
        val expected = BatteryInfo(
            level = 82,
            isCharging = true,
            pluggedType = PluggedType.AC,
            health = BatteryHealth.GOOD,
            temperatureCelsius = 28.5f,
            voltageMilliVolts = 4150
        )

        val repository = FakeBatteryRepository(expected)
        val useCase = ObserveBatteryInfoUseCase(repository)

        val result = useCase().first()

        assertEquals(82, result.level)
        assertTrue(result.isCharging)
        assertEquals(PluggedType.AC, result.pluggedType)
        assertEquals(4.15f, result.voltageVolts, 0.01f)
    }
}
