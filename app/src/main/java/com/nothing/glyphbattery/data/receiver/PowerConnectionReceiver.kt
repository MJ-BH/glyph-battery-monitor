package com.nothing.glyphbattery.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nothing.glyphbattery.GlyphBatteryApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for power connection events.
 * Triggers a momentary Glyph battery level pulse when the phone is plugged in.
 */
class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? GlyphBatteryApp ?: return
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                CoroutineScope(Dispatchers.Default).launch {
                    val batteryInfo = app.batteryRepository.getBatteryInfoSnapshot()
                    val glyphState = app.glyphRepository.glyphState.value
                    if (glyphState.flashOnPlugIn) {
                        app.glyphRepository.triggerBatteryFlash(batteryInfo.level)
                    }
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                CoroutineScope(Dispatchers.Default).launch {
                    app.glyphRepository.closeSession()
                }
            }
        }
    }
}
