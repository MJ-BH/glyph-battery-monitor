package com.nothing.glyphbattery.domain.model

enum class GlyphAnimationMode(
    val title: String,
    val description: String
) {
    PROGRESS_BAR(
        title = "Battery Meter",
        description = "Illuminates Glyph segments proportional to battery percentage (0-100%)"
    ),
    BREATHING_CHARGING(
        title = "Breathing Glow",
        description = "Smoothly pulses Glyph LEDs while phone is actively charging"
    ),
    PULSE_ALERT(
        title = "Pulse Flash",
        description = "Rhythmic double-pulse to signal charging connection or battery milestone"
    ),
    STEADY_ON(
        title = "Steady Light",
        description = "Keeps relevant Glyph channels illuminated"
    ),
    OFF(
        title = "Disabled",
        description = "Glyph LEDs remain dark"
    )
}
