package com.nothing.glyphbattery.data.ble

import android.content.Context
import com.nothing.glyphbattery.domain.model.CmfWatchMetrics
import com.nothing.glyphbattery.domain.repository.CmfWatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

class CmfWatchRepositoryImpl(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : CmfWatchRepository {

    private val bleManager = CmfBleManager(context, scope)
    override val watchMetrics: StateFlow<CmfWatchMetrics> = bleManager.metrics

    override fun startScanningAndConnect() {
        bleManager.startScanAndConnect()
    }

    override fun disconnect() {
        bleManager.disconnect()
    }

    override fun triggerSimulatedWristGesture() {
        bleManager.triggerWristGesture()
    }

    override fun setSimulatedHeartRate(bpm: Int) {
        bleManager.setSimulatedHeartRate(bpm)
    }
}
