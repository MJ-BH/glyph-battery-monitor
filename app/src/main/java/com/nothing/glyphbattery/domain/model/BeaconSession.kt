package com.nothing.glyphbattery.domain.model

data class BeaconSession(
    val speedKmh: Float = 0.0f,
    val isBrakingDecelerating: Boolean = false,
    val activeMode: GlyphBeaconMode = GlyphBeaconMode.BIKE_SMART_STROBE,
    val currentBpm: Int = 78,
    val heartRateZone: HeartRateZone = HeartRateZone.RESTING,
    val isStrobingActive: Boolean = false,
    val currentStrobePulseAlpha: Float = 0.0f,
    val morseTransmissionText: String? = null,
    val isMorseTransmitting: Boolean = false,
    val isClapDetectorListening: Boolean = false,
    val lastClapTriggerTimestamp: Long = 0L,
    val sessionDurationSeconds: Long = 0L
)
