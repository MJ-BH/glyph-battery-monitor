package com.nothing.glyphbattery.data.mapper

import android.os.BatteryManager
import com.nothing.glyphbattery.data.dto.BatteryInfoDto
import com.nothing.glyphbattery.domain.model.BatteryHealth
import com.nothing.glyphbattery.domain.model.PluggedType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BatteryMapperTest {

    private lateinit var mapper: BatteryMapper

    @Before
    fun setup() {
        mapper = BatteryMapper()
    }

    @Test
    fun `mapToDomain correctly maps BatteryInfoDto to BatteryInfo`() {
        val dto = BatteryInfoDto(
            level = 85,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_CHARGING,
            plugged = BatteryManager.BATTERY_PLUGGED_USB,
            health = BatteryManager.BATTERY_HEALTH_GOOD,
            temperature = 315,
            voltage = 4200,
            technology = "Li-poly",
            present = true
        )

        val domain = mapper.mapToDomain(dto)

        assertEquals(85, domain.level)
        assertTrue(domain.isCharging)
        assertEquals(PluggedType.USB, domain.pluggedType)
        assertEquals(BatteryHealth.GOOD, domain.health)
        assertEquals(31.5f, domain.temperatureCelsius, 0.01f)
        assertEquals(4200, domain.voltageMilliVolts)
        assertEquals(4.20f, domain.voltageVolts, 0.01f)
        assertEquals("Li-poly", domain.technology)
    }

    @Test
    fun `mapToDomain handles unknown plugged type and health safely`() {
        val dto = BatteryInfoDto(
            level = 42,
            scale = 100,
            status = BatteryManager.BATTERY_STATUS_DISCHARGING,
            plugged = 0,
            health = 999,
            temperature = 250,
            voltage = 3800,
            technology = null,
            present = true
        )

        val domain = mapper.mapToDomain(dto)

        assertEquals(42, domain.level)
        assertEquals(PluggedType.NONE, domain.pluggedType)
        assertEquals(BatteryHealth.UNKNOWN, domain.health)
        assertEquals("Li-ion", domain.technology)
    }
}
