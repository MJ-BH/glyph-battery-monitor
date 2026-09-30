package com.nothing.glyphbattery

import android.app.Application
import com.nothing.glyphbattery.data.battery.BatteryDataSource
import com.nothing.glyphbattery.data.battery.BatteryRepositoryImpl
import com.nothing.glyphbattery.data.glyph.GlyphRepositoryImpl
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphUseCase
import com.nothing.glyphbattery.domain.usecase.GetBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.GetNothingDeviceUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerGlyphBatteryFlashUseCase

class GlyphBatteryApp : Application() {

    lateinit var batteryRepository: BatteryRepository
        private set

    lateinit var glyphRepository: GlyphRepository
        private set

    lateinit var observeBatteryInfoUseCase: ObserveBatteryInfoUseCase
        private set

    lateinit var getBatteryInfoUseCase: GetBatteryInfoUseCase
        private set

    lateinit var controlGlyphUseCase: ControlGlyphUseCase
        private set

    lateinit var triggerGlyphBatteryFlashUseCase: TriggerGlyphBatteryFlashUseCase
        private set

    lateinit var getNothingDeviceUseCase: GetNothingDeviceUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        val batteryDataSource = BatteryDataSource(this)
        batteryRepository = BatteryRepositoryImpl(batteryDataSource)
        glyphRepository = GlyphRepositoryImpl(this)

        observeBatteryInfoUseCase = ObserveBatteryInfoUseCase(batteryRepository)
        getBatteryInfoUseCase = GetBatteryInfoUseCase(batteryRepository)
        controlGlyphUseCase = ControlGlyphUseCase(glyphRepository)
        triggerGlyphBatteryFlashUseCase = TriggerGlyphBatteryFlashUseCase(batteryRepository, glyphRepository)
        getNothingDeviceUseCase = GetNothingDeviceUseCase(glyphRepository)
    }
}
