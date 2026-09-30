package com.nothing.glyphbattery.data.mapper

import com.nothing.glyphbattery.data.dto.GlyphStateDto
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.model.GlyphState
import com.nothing.glyphbattery.domain.model.NothingDeviceModel

class GlyphMapper {

    fun mapToDomain(dto: GlyphStateDto): GlyphState {
        val mode = runCatching { GlyphAnimationMode.valueOf(dto.activeModeName) }
            .getOrDefault(GlyphAnimationMode.PROGRESS_BAR)

        val device = NothingDeviceModel.fromModelCode(dto.detectedModelCode)

        return GlyphState(
            isConnected = dto.isConnected,
            isSessionOpen = dto.isSessionOpen,
            activeMode = mode,
            currentProgress = dto.currentProgress,
            activeChannels = dto.activeChannels.toSet(),
            deviceModel = device,
            syncWithCharging = dto.syncWithCharging,
            flashOnPlugIn = dto.flashOnPlugIn,
            lastFlashedTimestamp = dto.lastFlashedTimestamp,
            isGenuineHardware = dto.isGenuineHardware,
            hardwareModelName = dto.hardwareModelName
        )
    }

    fun mapToDto(domain: GlyphState): GlyphStateDto {
        return GlyphStateDto(
            isConnected = domain.isConnected,
            isSessionOpen = domain.isSessionOpen,
            activeModeName = domain.activeMode.name,
            currentProgress = domain.currentProgress,
            activeChannels = domain.activeChannels.toList(),
            detectedModelCode = domain.deviceModel.modelCode,
            syncWithCharging = domain.syncWithCharging,
            flashOnPlugIn = domain.flashOnPlugIn,
            lastFlashedTimestamp = domain.lastFlashedTimestamp,
            isGenuineHardware = domain.isGenuineHardware,
            hardwareModelName = domain.hardwareModelName
        )
    }
}
