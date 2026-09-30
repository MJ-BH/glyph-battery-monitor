package com.nothing.glyphbattery.presentation.ui.dashboard

import com.nothing.glyphbattery.domain.model.BatteryHealth
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.model.PluggedType
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerGlyphBatteryFlashUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestBatteryRepo : BatteryRepository {
        val info = BatteryInfo(level = 75, isCharging = true, pluggedType = PluggedType.AC)
        override fun observeBatteryInfo(): Flow<BatteryInfo> = flowOf(info)
        override fun getBatteryInfoSnapshot(): BatteryInfo = info
    }

    private class TestGlyphRepo : GlyphRepository {
        val state = MutableStateFlow(GlyphState(deviceModel = NothingDeviceModel.PHONE_2A))
        override val glyphState: StateFlow<GlyphState> = state
        var lastMode: GlyphAnimationMode? = null
        var syncCharging: Boolean = true

        override suspend fun initialize() {}
        override suspend fun openSession(): Boolean = true
        override suspend fun closeSession() {}
        override suspend fun displayProgress(progress: Int, reverse: Boolean) {}
        override suspend fun setAnimationMode(mode: GlyphAnimationMode) {
            lastMode = mode
        }
        override suspend fun triggerBatteryFlash(batteryLevel: Int) {}
        override suspend fun setSyncWithCharging(enabled: Boolean) {
            syncCharging = enabled
        }
        override suspend fun setDeviceModel(model: NothingDeviceModel) {
            state.value = state.value.copy(deviceModel = model)
        }
        override fun release() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `event ChangeAnimationMode updates glyph animation mode`() = runTest {
        val batteryRepo = TestBatteryRepo()
        val glyphRepo = TestGlyphRepo()

        val observeUseCase = ObserveBatteryInfoUseCase(batteryRepo)
        val controlUseCase = ControlGlyphUseCase(glyphRepo)
        val flashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo)

        val viewModel = BatteryDashboardViewModel(
            observeBatteryInfoUseCase = observeUseCase,
            controlGlyphUseCase = controlUseCase,
            triggerGlyphBatteryFlashUseCase = flashUseCase,
            glyphRepository = glyphRepo
        )

        advanceUntilIdle()

        viewModel.onEvent(BatteryDashboardEvent.ChangeAnimationMode(GlyphAnimationMode.BREATHING_CHARGING))
        advanceUntilIdle()

        assertEquals(GlyphAnimationMode.BREATHING_CHARGING, glyphRepo.lastMode)
    }

    @Test
    fun `event SelectDeviceModel updates active device model`() = runTest {
        val batteryRepo = TestBatteryRepo()
        val glyphRepo = TestGlyphRepo()

        val viewModel = BatteryDashboardViewModel(
            observeBatteryInfoUseCase = ObserveBatteryInfoUseCase(batteryRepo),
            controlGlyphUseCase = ControlGlyphUseCase(glyphRepo),
            triggerGlyphBatteryFlashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo),
            glyphRepository = glyphRepo
        )

        advanceUntilIdle()

        viewModel.onEvent(BatteryDashboardEvent.SelectDeviceModel(NothingDeviceModel.PHONE_2))
        advanceUntilIdle()

        assertEquals(NothingDeviceModel.PHONE_2, glyphRepo.state.value.deviceModel)
    }
}
