package com.nothing.glyphbattery.presentation.ui.dashboard

import com.nothing.glyphbattery.domain.model.BeaconSession
import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import com.nothing.glyphbattery.domain.model.GlyphBeaconMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.BeaconRepository
import com.nothing.glyphbattery.domain.repository.CmfWatchRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphBeaconUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBeaconMetricsUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveCmfHeartRateUseCase
import com.nothing.glyphbattery.domain.usecase.TransmitMorseSosUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerFindPhoneStrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class BeaconCompanionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeBeaconRepo : BeaconRepository {
        val state = MutableStateFlow(BeaconSession())
        override val sessionState: StateFlow<BeaconSession> = state

        override suspend fun setBeaconMode(mode: GlyphBeaconMode) {
            state.value = state.value.copy(activeMode = mode)
        }
        override suspend fun startSmartBikeBeacon() {}
        override suspend fun stopBeacon() {}
        override suspend fun transmitMorseMessage(text: String) {
            state.value = state.value.copy(isMorseTransmitting = true, morseTransmissionText = text)
        }
        override suspend fun cancelMorseTransmission() {
            state.value = state.value.copy(isMorseTransmitting = false)
        }
        override suspend fun triggerEmergencyFindStrobe() {}
        override fun setClapDetectorEnabled(enabled: Boolean) {}
        override fun updateSimulatedSpeed(speedKmh: Float, isBraking: Boolean) {
            state.value = state.value.copy(speedKmh = speedKmh, isBrakingDecelerating = isBraking)
        }
    }

    private class FakeCmfRepo : CmfWatchRepository {
        val metrics = MutableStateFlow(CmfWatchMetrics(heartRateBpm = 88))
        override val watchMetrics: StateFlow<CmfWatchMetrics> = metrics

        override fun startScanningAndConnect() {}
        override fun disconnect() {}
        override fun triggerSimulatedWristGesture() {}
        override fun setSimulatedHeartRate(bpm: Int) {
            metrics.value = metrics.value.copy(heartRateBpm = bpm)
        }
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
    fun `event SelectBeaconMode updates active mode in repository`() = runTest {
        val beaconRepo = FakeBeaconRepo()
        val cmfRepo = FakeCmfRepo()

        val viewModel = BeaconCompanionViewModel(
            observeBeaconMetricsUseCase = ObserveBeaconMetricsUseCase(beaconRepo),
            observeCmfHeartRateUseCase = ObserveCmfHeartRateUseCase(cmfRepo),
            controlGlyphBeaconUseCase = ControlGlyphBeaconUseCase(beaconRepo),
            transmitMorseSosUseCase = TransmitMorseSosUseCase(beaconRepo),
            triggerFindPhoneStrobeUseCase = TriggerFindPhoneStrobeUseCase(beaconRepo)
        )

        advanceUntilIdle()

        viewModel.onEvent(BeaconCompanionEvent.SelectBeaconMode(GlyphBeaconMode.HEART_BIO_PULSE))
        advanceUntilIdle()

        assertEquals(GlyphBeaconMode.HEART_BIO_PULSE, beaconRepo.state.value.activeMode)
    }

    @Test
    fun `event TransmitSos triggers Morse transmission`() = runTest {
        val beaconRepo = FakeBeaconRepo()
        val cmfRepo = FakeCmfRepo()

        val viewModel = BeaconCompanionViewModel(
            observeBeaconMetricsUseCase = ObserveBeaconMetricsUseCase(beaconRepo),
            observeCmfHeartRateUseCase = ObserveCmfHeartRateUseCase(cmfRepo),
            controlGlyphBeaconUseCase = ControlGlyphBeaconUseCase(beaconRepo),
            transmitMorseSosUseCase = TransmitMorseSosUseCase(beaconRepo),
            triggerFindPhoneStrobeUseCase = TriggerFindPhoneStrobeUseCase(beaconRepo)
        )

        advanceUntilIdle()

        viewModel.onEvent(BeaconCompanionEvent.TransmitSos)
        advanceUntilIdle()

        assertEquals("SOS", beaconRepo.state.value.morseTransmissionText)
        assertEquals(true, beaconRepo.state.value.isMorseTransmitting)
    }
}
