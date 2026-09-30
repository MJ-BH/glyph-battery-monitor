package com.nothing.glyphbattery.di

import com.nothing.glyphbattery.data.mapper.BatteryMapper
import com.nothing.glyphbattery.data.mapper.GlyphMapper
import com.nothing.glyphbattery.data.repository.BatteryRepositoryImpl
import com.nothing.glyphbattery.data.repository.GlyphRepositoryImpl
import com.nothing.glyphbattery.data.source.BatteryDataSource
import com.nothing.glyphbattery.data.source.GlyphManagerBridge
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphUseCase
import com.nothing.glyphbattery.domain.usecase.GetBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.GetNothingDeviceUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBatteryInfoUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerGlyphBatteryFlashUseCase
import com.nothing.glyphbattery.ui.battery.BatteryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Mappers
    single { BatteryMapper() }
    single { GlyphMapper() }

    // Data Sources
    single { BatteryDataSource(androidContext()) }
    single { GlyphManagerBridge(androidContext()) }
    single { com.nothing.glyphbattery.data.source.FlipOrientationSensor(androidContext()) }

    // Repositories
    single<BatteryRepository> {
        BatteryRepositoryImpl(
            batteryDataSource = get(),
            batteryMapper = get()
        )
    }

    single<GlyphRepository> {
        GlyphRepositoryImpl(
            bridge = get(),
            flipOrientationSensor = get(),
            mapper = get()
        )
    }

    // Use Cases
    single { ObserveBatteryInfoUseCase(batteryRepository = get()) }
    single { GetBatteryInfoUseCase(batteryRepository = get()) }
    single { ControlGlyphUseCase(glyphRepository = get()) }
    single { TriggerGlyphBatteryFlashUseCase(batteryRepository = get(), glyphRepository = get()) }
    single { GetNothingDeviceUseCase(glyphRepository = get()) }

    // ViewModels
    viewModel {
        BatteryViewModel(
            observeBatteryInfoUseCase = get(),
            controlGlyphUseCase = get(),
            triggerGlyphBatteryFlashUseCase = get(),
            glyphRepository = get()
        )
    }
}
