package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.core.result.Result
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerGlyphBatteryFlashUseCaseTest {

    private class FakeBatteryRepository(private val level: Int) : BatteryRepository {
        override fun observeBatteryInfo(): Flow<BatteryInfo> = flowOf(BatteryInfo(level = level))
        override suspend fun getBatteryInfoSnapshot(): Result<BatteryInfo, Throwable> = Result.Success(BatteryInfo(level = level))
    }

    private class FakeGlyphRepository : GlyphRepository {
        val state = MutableStateFlow(GlyphState())
        override val glyphState: StateFlow<GlyphState> = state
        var flashedLevel: Int? = null

        override suspend fun initialize() {}
        override suspend fun openSession(): Boolean = true
        override suspend fun closeSession() {}
        override suspend fun displayProgress(progress: Int, reverse: Boolean) {}
        override suspend fun setAnimationMode(mode: GlyphAnimationMode) {}
        override suspend fun triggerBatteryFlash(batteryLevel: Int) {
            flashedLevel = batteryLevel
        }
        override suspend fun setSyncWithCharging(enabled: Boolean) {}
        override suspend fun setFlipToGlyphCharging(enabled: Boolean) {}
        override fun observeFaceDown(): Flow<Boolean> = flowOf(false)
        override suspend fun turnOff() {}
        override suspend fun setDeviceModel(model: NothingDeviceModel) {}
        override fun release() {}
    }

    @Test
    fun `triggerBatteryFlash flashes current battery level to Glyph LEDs`() = runTest {
        val batteryRepo = FakeBatteryRepository(level = 67)
        val glyphRepo = FakeGlyphRepository()
        val useCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo)

        useCase()

        assertEquals(67, glyphRepo.flashedLevel)
    }
}
