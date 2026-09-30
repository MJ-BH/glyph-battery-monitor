package com.nothing.glyphbattery.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.nothing.glyphbattery.data.dto.BatteryInfoDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryDataSource(
    private val context: Context
) {
    fun observeBatteryDto(): Flow<BatteryInfoDto> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val dto = parseBatteryIntentToDto(it)
                    trySend(dto)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        val stickyIntent = context.registerReceiver(receiver, filter)
        if (stickyIntent != null) {
            trySend(parseBatteryIntentToDto(stickyIntent))
        } else {
            trySend(getImmediateSnapshotDto())
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    fun getImmediateSnapshotDto(): BatteryInfoDto {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        return if (intent != null) {
            parseBatteryIntentToDto(intent)
        } else {
            BatteryInfoDto(
                level = 50,
                scale = 100,
                status = BatteryManager.BATTERY_STATUS_DISCHARGING,
                plugged = 0,
                health = BatteryManager.BATTERY_HEALTH_GOOD,
                temperature = 250,
                voltage = 3800,
                technology = "Li-ion",
                present = true
            )
        }
    }

    private fun parseBatteryIntentToDto(intent: Intent): BatteryInfoDto {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 3800)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

        var currentMicroAmps: Int? = null
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (batteryManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            }
        } catch (_: Exception) {}

        return BatteryInfoDto(
            level = level,
            scale = scale,
            status = status,
            plugged = plugged,
            health = health,
            temperature = temperature,
            voltage = voltage,
            technology = technology,
            present = present,
            currentMicroAmps = currentMicroAmps
        )
    }
}
