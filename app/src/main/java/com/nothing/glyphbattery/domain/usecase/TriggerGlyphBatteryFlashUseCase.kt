package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository

/**
 * UseCase to trigger an interactive battery level flash on the physical Glyph LEDs.
 * Perfect for App Shortcuts, App Widgets, and manual UI trigger buttons.
 */
class TriggerGlyphBatteryFlashUseCase(
    private val batteryRepository: BatteryRepository,
    private val glyphRepository: GlyphRepository
) {
    suspend operator fun invoke() {
        val batteryInfo = batteryRepository.getBatteryInfoSnapshot()
        glyphRepository.triggerBatteryFlash(batteryInfo.level)
    }

    suspend operator fun invoke(overrideLevel: Int) {
        glyphRepository.triggerBatteryFlash(overrideLevel)
    }
}
