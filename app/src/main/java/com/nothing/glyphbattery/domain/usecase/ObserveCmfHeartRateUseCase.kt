package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import com.nothing.glyphbattery.domain.repository.CmfWatchRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCmfHeartRateUseCase(
    private val cmfWatchRepository: CmfWatchRepository
) {
    operator fun invoke(): StateFlow<CmfWatchMetrics> = cmfWatchRepository.watchMetrics

    fun startScanning() = cmfWatchRepository.startScanningAndConnect()
    fun disconnect() = cmfWatchRepository.disconnect()
    fun setSimulatedHeartRate(bpm: Int) = cmfWatchRepository.setSimulatedHeartRate(bpm)
    fun triggerWristGesture() = cmfWatchRepository.triggerSimulatedWristGesture()
}
