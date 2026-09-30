package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.core.result.getOrNull
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository

class TriggerGlyphBatteryFlashUseCase(
    private val batteryRepository: BatteryRepository,
    private val glyphRepository: GlyphRepository
) {
    suspend operator fun invoke() {
        val result = batteryRepository.getBatteryInfoSnapshot()
        val level = result.getOrNull()?.level ?: 50
        glyphRepository.triggerBatteryFlash(level)
    }

    suspend operator fun invoke(overrideLevel: Int) {
        glyphRepository.triggerBatteryFlash(overrideLevel)
    }
}
