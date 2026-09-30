package com.nothing.glyphbattery.ui.battery

import com.nothing.glyphbattery.core.result.Result
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
import com.nothing.glyphbattery.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeBatteryRepo(private val batteryInfo: BatteryInfo) : BatteryRepository {
        override fun observeBatteryInfo(): Flow<BatteryInfo> = flowOf(batteryInfo)
        override suspend fun getBatteryInfoSnapshot(): Result<BatteryInfo, Throwable> = Result.Success(batteryInfo)
    }

    private class FakeGlyphRepo : GlyphRepository {
        private val _state = MutableStateFlow(GlyphState(isConnected = true, deviceModel = NothingDeviceModel.PHONE_2))
        override val glyphState: StateFlow<GlyphState> = _state.asStateFlow()

        var lastProgress: Int? = null
        var lastMode: GlyphAnimationMode? = null

        override suspend fun initialize() {}
        override suspend fun openSession(): Boolean = true
        override suspend fun closeSession() {}
        override suspend fun displayProgress(progress: Int, reverse: Boolean) {
            lastProgress = progress
            _state.value = _state.value.copy(currentProgress = progress)
        }
        override suspend fun setAnimationMode(mode: GlyphAnimationMode) {
            lastMode = mode
            _state.value = _state.value.copy(activeMode = mode)
        }
        override suspend fun triggerBatteryFlash(batteryLevel: Int) {}
        override suspend fun setSyncWithCharging(enabled: Boolean) {
            _state.value = _state.value.copy(syncWithCharging = enabled)
        }
        override suspend fun setFlipToGlyphCharging(enabled: Boolean) {
            _state.value = _state.value.copy(flipToGlyphCharging = enabled)
        }
        override fun observeFaceDown(): Flow<Boolean> = flowOf(false)
        override suspend fun turnOff() {}
        override suspend fun setDeviceModel(model: NothingDeviceModel) {
            _state.value = _state.value.copy(deviceModel = model)
        }
        override fun release() {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel combines battery and glyph state into Success UiState`() = runTest {
        val fakeBattery = BatteryInfo(
            level = 90,
            isCharging = true,
            pluggedType = PluggedType.WIRELESS,
            health = BatteryHealth.GOOD,
            temperatureCelsius = 30.0f,
            voltageMilliVolts = 4100
        )
        val batteryRepo = FakeBatteryRepo(fakeBattery)
        val glyphRepo = FakeGlyphRepo()

        val observeBatteryUseCase = ObserveBatteryInfoUseCase(batteryRepo)
        val controlGlyphUseCase = ControlGlyphUseCase(glyphRepo)
        val flashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo)

        val viewModel = BatteryViewModel(
            observeBatteryInfoUseCase = observeBatteryUseCase,
            controlGlyphUseCase = controlGlyphUseCase,
            triggerGlyphBatteryFlashUseCase = flashUseCase,
            glyphRepository = glyphRepo
        )

        val collectJob = launch(StandardTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState is UiState.Success)
        val successData = (currentState as UiState.Success).data

        assertEquals(90, successData.batteryInfo.level)
        assertTrue(successData.batteryInfo.isCharging)
        assertEquals(NothingDeviceModel.PHONE_2, successData.glyphState.deviceModel)

        collectJob.cancel()
    }

    @Test
    fun `onEvent ChangeAnimationMode updates glyph animation mode`() = runTest {
        val fakeBattery = BatteryInfo(level = 75, isCharging = false)
        val batteryRepo = FakeBatteryRepo(fakeBattery)
        val glyphRepo = FakeGlyphRepo()

        val viewModel = BatteryViewModel(
            observeBatteryInfoUseCase = ObserveBatteryInfoUseCase(batteryRepo),
            controlGlyphUseCase = ControlGlyphUseCase(glyphRepo),
            triggerGlyphBatteryFlashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo),
            glyphRepository = glyphRepo
        )

        viewModel.onEvent(BatteryUiEvent.ChangeAnimationMode(GlyphAnimationMode.PULSE_ALERT))
        advanceUntilIdle()

        assertEquals(GlyphAnimationMode.PULSE_ALERT, glyphRepo.lastMode)
    }

    @Test
    fun `onEvent SelectDeviceModel switches simulated model`() = runTest {
        val fakeBattery = BatteryInfo(level = 60, isCharging = false)
        val batteryRepo = FakeBatteryRepo(fakeBattery)
        val glyphRepo = FakeGlyphRepo()

        val viewModel = BatteryViewModel(
            observeBatteryInfoUseCase = ObserveBatteryInfoUseCase(batteryRepo),
            controlGlyphUseCase = ControlGlyphUseCase(glyphRepo),
            triggerGlyphBatteryFlashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepo, glyphRepo),
            glyphRepository = glyphRepo
        )

        viewModel.onEvent(BatteryUiEvent.SelectDeviceModel(NothingDeviceModel.PHONE_2A))
        advanceUntilIdle()

        assertEquals(NothingDeviceModel.PHONE_2A, glyphRepo.glyphState.value.deviceModel)
    }
}
