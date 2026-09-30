package com.nothing.glyphbattery.domain.model

/**
 * Live state of the Glyph hardware and UI simulation.
 */
data class GlyphState(
    val isConnected: Boolean = false,
    val isSessionOpen: Boolean = false,
    val activeMode: GlyphAnimationMode = GlyphAnimationMode.PROGRESS_BAR,
    val currentProgress: Int = 0, // 0 to 100
    val activeChannels: Set<String> = emptySet(),
    val brightnessPercent: Int = 100,
    val deviceModel: NothingDeviceModel = NothingDeviceModel.PHONE_2A,
    val syncWithCharging: Boolean = true,
    val flashOnPlugIn: Boolean = true,
    val lastFlashedTimestamp: Long = 0L,
    val isGenuineHardware: Boolean = false,
    val hardwareModelName: String = ""
)
