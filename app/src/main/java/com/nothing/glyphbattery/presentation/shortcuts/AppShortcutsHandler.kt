package com.nothing.glyphbattery.presentation.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.nothing.glyphbattery.R
import com.nothing.glyphbattery.data.glyph.NothingGlyphConstants
import com.nothing.glyphbattery.presentation.MainActivity

object AppShortcutsHandler {

    const val SHORTCUT_FLASH_BATTERY = "shortcut_flash_battery"
    const val SHORTCUT_TOGGLE_GLOW = "shortcut_toggle_glow"

    fun publishDynamicShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

        val flashIntent = Intent(context, MainActivity::class.java).apply {
            action = NothingGlyphConstants.ACTION_FLASH_BATTERY
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val flashShortcut = ShortcutInfo.Builder(context, SHORTCUT_FLASH_BATTERY)
            .setShortLabel("Flash Glyph")
            .setLongLabel("Flash Battery Level on Glyph")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_flash))
            .setIntent(flashIntent)
            .setRank(1)
            .build()

        val toggleIntent = Intent(context, MainActivity::class.java).apply {
            action = NothingGlyphConstants.ACTION_TOGGLE_CHARGING_GLOW
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val toggleShortcut = ShortcutInfo.Builder(context, SHORTCUT_TOGGLE_GLOW)
            .setShortLabel("Toggle Glow")
            .setLongLabel("Toggle Glyph Charging Glow")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_charging))
            .setIntent(toggleIntent)
            .setRank(2)
            .build()

        try {
            shortcutManager.dynamicShortcuts = listOf(flashShortcut, toggleShortcut)
        } catch (_: Exception) {}
    }
}
