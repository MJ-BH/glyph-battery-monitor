package com.nothing.glyphbattery.presentation.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.nothing.glyphbattery.R
import com.nothing.glyphbattery.presentation.MainActivity

object AppShortcutsHandler {

    const val SHORTCUT_BIKE_BEACON = "shortcut_bike_beacon"
    const val SHORTCUT_HEART_SYNC = "shortcut_heart_sync"
    const val SHORTCUT_EMERGENCY_SOS = "shortcut_emergency_sos"

    fun publishDynamicShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

        val bikeIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_START_BIKE_BEACON"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val bikeShortcut = ShortcutInfo.Builder(context, SHORTCUT_BIKE_BEACON)
            .setShortLabel("Bike Beacon")
            .setLongLabel("Start Smart Bike Safety Beacon")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_flash))
            .setIntent(bikeIntent)
            .setRank(1)
            .build()

        val heartIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_START_HEART_SYNC"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val heartShortcut = ShortcutInfo.Builder(context, SHORTCUT_HEART_SYNC)
            .setShortLabel("CMF Heart Sync")
            .setLongLabel("Mirror Wrist Heart Rate on Glyphs")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_health))
            .setIntent(heartIntent)
            .setRank(2)
            .build()

        val sosIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_TRIGGER_SOS"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val sosShortcut = ShortcutInfo.Builder(context, SHORTCUT_EMERGENCY_SOS)
            .setShortLabel("Morse SOS")
            .setLongLabel("Transmit Emergency Optical SOS")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_charging))
            .setIntent(sosIntent)
            .setRank(3)
            .build()

        try {
            shortcutManager.dynamicShortcuts = listOf(bikeShortcut, heartShortcut, sosShortcut)
        } catch (_: Exception) {}
    }
}
