package com.nothing.glyphbattery.data.mapper

import android.os.BatteryManager
import com.nothing.glyphbattery.data.dto.BatteryInfoDto
import com.nothing.glyphbattery.domain.model.BatteryHealth
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.PluggedType

class BatteryMapper {

    fun mapToDomain(dto: BatteryInfoDto): BatteryInfo {
        val pct = if (dto.level >= 0 && dto.scale > 0) {
            ((dto.level.toFloat() / dto.scale.toFloat()) * 100).toInt()
        } else {
            0
        }

        val isCharging = dto.status == BatteryManager.BATTERY_STATUS_CHARGING ||
                dto.status == BatteryManager.BATTERY_STATUS_FULL

        val pluggedType = when (dto.plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PluggedType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PluggedType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedType.WIRELESS
            4 -> PluggedType.DOCK
            else -> PluggedType.NONE
        }

        val health = when (dto.health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        val speedWatts = if (isCharging && dto.currentMicroAmps != null && dto.currentMicroAmps > 0) {
            (dto.currentMicroAmps / 1_000_000f) * (dto.voltage / 1000f)
        } else {
            null
        }

        return BatteryInfo(
            level = pct.coerceIn(0, 100),
            isCharging = isCharging,
            pluggedType = pluggedType,
            health = health,
            temperatureCelsius = dto.temperature / 10.0f,
            voltageMilliVolts = dto.voltage,
            technology = dto.technology ?: "Li-ion",
            present = dto.present,
            chargingSpeedWatts = speedWatts
        )
    }
}
