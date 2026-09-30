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
        android.util.Log.i("PowerConnectionReceiver", "Received broadcast action: $action")

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        glyphRepository.initialize()
                        kotlinx.coroutines.delay(250)
                        val batteryResult = batteryRepository.getBatteryInfo()
                        val batteryLevel = batteryResult.getOrNull()?.level ?: 50
                        val glyphState = glyphRepository.glyphState.value
                        if (glyphState.flashOnPlugIn) {
                            android.util.Log.i("PowerConnectionReceiver", "Flashing Glyph on cable plug-in: $batteryLevel%")
                            glyphRepository.triggerBatteryFlash(batteryLevel)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PowerConnectionReceiver", "Error flashing Glyph: ${e.message}")
                    } finally {
                        pending.finish()
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
