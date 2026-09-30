package com.nothing.glyphbattery

import android.app.Application
import com.nothing.glyphbattery.data.beacon.BeaconRepositoryImpl
import com.nothing.glyphbattery.data.ble.CmfWatchRepositoryImpl
import com.nothing.glyphbattery.domain.repository.BeaconRepository
import com.nothing.glyphbattery.domain.repository.CmfWatchRepository
import com.nothing.glyphbattery.domain.usecase.ControlGlyphBeaconUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveBeaconMetricsUseCase
import com.nothing.glyphbattery.domain.usecase.ObserveCmfHeartRateUseCase
import com.nothing.glyphbattery.domain.usecase.TransmitMorseSosUseCase
import com.nothing.glyphbattery.domain.usecase.TriggerFindPhoneStrobeUseCase

class GlyphBatteryApp : Application() {

    lateinit var cmfWatchRepository: CmfWatchRepository
        private set

    lateinit var beaconRepository: BeaconRepository
        private set

    lateinit var observeBeaconMetricsUseCase: ObserveBeaconMetricsUseCase
        private set

    lateinit var observeCmfHeartRateUseCase: ObserveCmfHeartRateUseCase
        private set

    lateinit var controlGlyphBeaconUseCase: ControlGlyphBeaconUseCase
        private set

    lateinit var transmitMorseSosUseCase: TransmitMorseSosUseCase
        private set

    lateinit var triggerFindPhoneStrobeUseCase: TriggerFindPhoneStrobeUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        val cmfImpl = CmfWatchRepositoryImpl(this)
        cmfWatchRepository = cmfImpl
        beaconRepository = BeaconRepositoryImpl(this, cmfImpl)

        observeBeaconMetricsUseCase = ObserveBeaconMetricsUseCase(beaconRepository)
        observeCmfHeartRateUseCase = ObserveCmfHeartRateUseCase(cmfWatchRepository)
        controlGlyphBeaconUseCase = ControlGlyphBeaconUseCase(beaconRepository)
        transmitMorseSosUseCase = TransmitMorseSosUseCase(beaconRepository)
        triggerFindPhoneStrobeUseCase = TriggerFindPhoneStrobeUseCase(beaconRepository)
    }
}
