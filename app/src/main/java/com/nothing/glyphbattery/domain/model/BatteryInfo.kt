package com.nothing.glyphbattery.domain.model

/**
 * Domain model representing battery and charging metrics.
 */
data class BatteryInfo(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val pluggedType: PluggedType = PluggedType.NONE,
    val health: BatteryHealth = BatteryHealth.GOOD,
    val temperatureCelsius: Float = 25.0f,
    val voltageMilliVolts: Int = 3800,
    val technology: String = "Li-ion",
    val present: Boolean = true,
    val chargingSpeedWatts: Float? = null
) {
    val voltageVolts: Float
        get() = voltageMilliVolts / 1000f

    val isFull: Boolean
        get() = level >= 100

    val isLow: Boolean
        get() = level <= 20 && !isCharging
}

enum class PluggedType {
    NONE,
    AC,
    USB,
    WIRELESS,
    DOCK
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}
