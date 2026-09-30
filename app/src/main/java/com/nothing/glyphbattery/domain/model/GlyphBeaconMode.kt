package com.nothing.glyphbattery.domain.model

enum class GlyphBeaconMode(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    BIKE_SMART_STROBE(
        title = "Smart Bike Beacon",
        subtitle = "Speed-adaptive safety strobe + auto brake light on deceleration",
        badge = "CYC"
    ),
    HEART_BIO_PULSE(
        title = "CMF Heart Bio-Pulse",
        subtitle = "Mirrors CMF Watch Pro 2 wrist heart rate directly on Glyph LEDs",
        badge = "HR"
    ),
    MORSE_OPTICAL_SOS(
        title = "Morse Optical SOS",
        subtitle = "Encodes text & SOS distress signals into optical LED Morse code",
        badge = "SOS"
    ),
    CLAP_DARK_FINDER(
        title = "Acoustic / Watch Finder",
        subtitle = "Ultra-bright strobe triggered by double-clap or CMF Watch 'Find Phone'",
        badge = "FIND"
    ),
    STEADY_REAR_LIGHT(
        title = "Steady Night Glow",
        subtitle = "Continuous high-visibility rear lighting for night walking/running",
        badge = "LGT"
    ),
    OFF(
        title = "Standby",
        subtitle = "Glyph LEDs inactive",
        badge = "OFF"
    )
}

enum class HeartRateZone(
    val zoneName: String,
    val minBpm: Int,
    val maxBpm: Int,
    val pulseCadenceMs: Long
) {
    RESTING("Resting / Recovery", 40, 99, 1000L),
    WARMUP("Warm Up Zone", 100, 119, 700L),
    FAT_BURN("Aerobic / Fat Burn", 120, 139, 500L),
    CARDIO("Anaerobic / Cardio", 140, 159, 380L),
    PEAK("Peak Stride / Extreme", 160, 220, 260L);

    companion object {
        fun fromBpm(bpm: Int): HeartRateZone {
            return when {
                bpm < 100 -> RESTING
                bpm in 100..119 -> WARMUP
                bpm in 120..139 -> FAT_BURN
                bpm in 140..159 -> CARDIO
                else -> PEAK
            }
        }
    }
}
