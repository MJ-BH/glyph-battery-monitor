package com.nothing.glyphbattery.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nothing.glyphbattery.core.result.getOrNull
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver for power connection events.
 * Triggers a momentary Glyph battery level pulse when the phone is plugged in.
 */
class PowerConnectionReceiver : BroadcastReceiver(), KoinComponent {

    private val batteryRepository: BatteryRepository by inject()
    private val glyphRepository: GlyphRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                CoroutineScope(Dispatchers.Default).launch {
                    val batteryResult = batteryRepository.getBatteryInfo()
                    val batteryLevel = batteryResult.getOrNull()?.level ?: 50
                    val glyphState = glyphRepository.glyphState.value
                    if (glyphState.flashOnPlugIn) {
                        glyphRepository.triggerBatteryFlash(batteryLevel)
                    }
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                CoroutineScope(Dispatchers.Default).launch {
                    glyphRepository.closeSession()
                }
            }
        }
    }
}
