package com.nothing.glyphbattery.domain.model

data class CmfWatchMetrics(
    val isConnected: Boolean = false,
    val deviceName: String = "CMF Watch Pro 2",
    val heartRateBpm: Int = 78,
    val batteryPercent: Int = 85,
    val isTrackingWorkout: Boolean = false,
    val lastGestureTimestamp: Long = 0L,
    val rssi: Int = -60
)
