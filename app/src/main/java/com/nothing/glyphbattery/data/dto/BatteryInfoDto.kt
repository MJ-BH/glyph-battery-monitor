package com.nothing.glyphbattery.data.dto

/**
 * Data Transfer Object representing raw system battery metrics.
 */
data class BatteryInfoDto(
    val level: Int,
    val scale: Int,
    val status: Int,
    val plugged: Int,
    val health: Int,
    val temperature: Int, // tenths of a degree Celsius
    val voltage: Int, // millivolts
    val technology: String?,
    val present: Boolean,
    val currentMicroAmps: Int? = null
)
