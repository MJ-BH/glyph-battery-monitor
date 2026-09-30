package com.nothing.glyphbattery.domain.model

/**
 * Supported Nothing Phone models and their respective Glyph LED architecture.
 */
enum class NothingDeviceModel(
    val displayName: String,
    val modelCode: String,
    val channelCount: Int,
    val hasProgressChannel: Boolean,
    val description: String
) {
    PHONE_1(
        displayName = "Nothing Phone (1)",
        modelCode = "20111",
        channelCount = 5,
        hasProgressChannel = true,
        description = "5 Glyph zones (A-E), D1 battery progress bar"
    ),
    PHONE_2(
        displayName = "Nothing Phone (2)",
        modelCode = "22111",
        channelCount = 33,
        hasProgressChannel = true,
        description = "33 Glyph zones, C1-C16 segmented progress bar & D1 indicator"
    ),
    PHONE_2A(
        displayName = "Nothing Phone (2a)",
        modelCode = "23111",
        channelCount = 3,
        hasProgressChannel = true,
        description = "3 Glyph ribbon strips surrounding camera module"
    ),
    PHONE_2A_PLUS(
        displayName = "Nothing Phone (2a) Plus",
        modelCode = "23113",
        channelCount = 3,
        hasProgressChannel = true,
        description = "3 Glyph ribbon strips with metallic accented aesthetic"
    ),
    PHONE_3A_SERIES(
        displayName = "Nothing Phone (3a / 4a Series)",
        modelCode = "24111",
        channelCount = 3,
        hasProgressChannel = true,
        description = "Next-gen Nothing Glyph Interface"
    ),
    VIRTUAL_SIMULATOR(
        displayName = "Nothing Device (Simulator Mode)",
        modelCode = "SIMULATOR",
        channelCount = 33,
        hasProgressChannel = true,
        description = "Interactive on-screen Nothing Glyph simulation"
    );

    companion object {
        fun fromModelCode(code: String): NothingDeviceModel {
            return entries.firstOrNull { it.modelCode.equals(code, ignoreCase = true) }
                ?: if (code.contains("2a", ignoreCase = true)) PHONE_2A
                else if (code.contains("phone", ignoreCase = true)) PHONE_2
                else VIRTUAL_SIMULATOR
        }
    }
}
