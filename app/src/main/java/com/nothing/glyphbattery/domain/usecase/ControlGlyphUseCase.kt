package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository

/**
 * UseCase to handle high-level control of Nothing Glyph animations and sessions.
 */
class ControlGlyphUseCase(
    private val glyphRepository: GlyphRepository
) {
    suspend fun openSession(): Boolean = glyphRepository.openSession()

    suspend fun closeSession() = glyphRepository.closeSession()

    suspend fun updateBatteryProgress(progress: Int) {
        glyphRepository.displayProgress(progress)
    }

    suspend fun setAnimationMode(mode: GlyphAnimationMode) {
        glyphRepository.setAnimationMode(mode)
    }

    suspend fun setSyncWithCharging(enabled: Boolean) {
        glyphRepository.setSyncWithCharging(enabled)
    }

    suspend fun setFlipToGlyphCharging(enabled: Boolean) {
        glyphRepository.setFlipToGlyphCharging(enabled)
    }

    suspend fun turnOff() {
        glyphRepository.turnOff()
    }

    suspend fun selectDeviceModel(model: NothingDeviceModel) {
        glyphRepository.setDeviceModel(model)
    }
}
