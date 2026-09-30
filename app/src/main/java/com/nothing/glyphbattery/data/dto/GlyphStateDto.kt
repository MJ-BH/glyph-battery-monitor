package com.nothing.glyphbattery.data.dto

/**
 * Data Transfer Object representing raw Nothing Glyph hardware state.
 */
data class GlyphStateDto(
    val isConnected: Boolean,
    val isSessionOpen: Boolean,
    val activeModeName: String,
    val currentProgress: Int,
    val activeChannels: List<String>,
    val detectedModelCode: String,
    val syncWithCharging: Boolean,
    val flashOnPlugIn: Boolean,
    val lastFlashedTimestamp: Long,
    val isGenuineHardware: Boolean = false,
    val hardwareModelName: String = ""
)
