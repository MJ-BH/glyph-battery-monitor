package com.nothing.glyphbattery.domain.usecase

import com.nothing.glyphbattery.domain.model.NothingDeviceModel
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * UseCase to obtain the detected or configured Nothing phone model.
 */
class GetNothingDeviceUseCase(
    private val glyphRepository: GlyphRepository
) {
    operator fun invoke(): Flow<NothingDeviceModel> {
        return glyphRepository.glyphState.map { it.deviceModel }
    }
}
