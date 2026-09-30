package com.nothing.glyphbattery.domain.repository

import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CmfWatchRepository {
    val watchMetrics: StateFlow<CmfWatchMetrics>

    fun startScanningAndConnect()
    fun disconnect()
    fun triggerSimulatedWristGesture()
    fun setSimulatedHeartRate(bpm: Int)
}
