package com.nothing.glyphbattery.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.nothing.glyphbattery.domain.model.BatteryHealth
import com.nothing.glyphbattery.domain.model.BatteryInfo
import com.nothing.glyphbattery.domain.model.PluggedType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryDataSource(
    private val context: Context
) {
    fun observeBattery(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val info = parseBatteryIntent(it)
                    trySend(info)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        // Register receiver and immediately emit current sticky intent
        val stickyIntent = context.registerReceiver(receiver, filter)
        if (stickyIntent != null) {
            trySend(parseBatteryIntent(stickyIntent))
        } else {
            trySend(getImmediateSnapshot())
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
                // Ignore receiver not registered
            }
        }
    }

    fun getImmediateSnapshot(): BatteryInfo {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        return if (intent != null) {
            parseBatteryIntent(intent)
        } else {
            BatteryInfo()
        }
    }

    private fun parseBatteryIntent(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            0
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> PluggedType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PluggedType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedType.WIRELESS
            4 -> PluggedType.DOCK // BATTERY_PLUGGED_DOCK
            else -> PluggedType.NONE
        }

        val healthExtra = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthExtra) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        val tempTenthsCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
        val temperature = tempTenthsCelsius / 10.0f

        val voltageMilliVolts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 3800)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

        var speedWatts: Float? = null
        if (isCharging) {
            try {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                if (batteryManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    if (currentMicroAmps > 0) {
                        val currentAmps = currentMicroAmps / 1_000_000f
                        val volts = voltageMilliVolts / 1000f
                        speedWatts = currentAmps * volts
                    }
                }
            } catch (_: Exception) {
                // Ignore current calculation errors
            }
        }

        return BatteryInfo(
            level = batteryPct.coerceIn(0, 100),
            isCharging = isCharging,
            pluggedType = pluggedType,
            health = health,
            temperatureCelsius = temperature,
            voltageMilliVolts = voltageMilliVolts,
            technology = technology,
            present = present,
            chargingSpeedWatts = speedWatts
        )
    }
}
