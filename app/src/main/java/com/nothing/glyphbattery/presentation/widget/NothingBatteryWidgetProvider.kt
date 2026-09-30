package com.nothing.glyphbattery.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import com.nothing.glyphbattery.GlyphBatteryApp
import com.nothing.glyphbattery.R
import com.nothing.glyphbattery.data.glyph.NothingGlyphConstants
import com.nothing.glyphbattery.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NothingBatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == NothingGlyphConstants.ACTION_FLASH_BATTERY) {
            val app = context.applicationContext as? GlyphBatteryApp
            if (app != null) {
                CoroutineScope(Dispatchers.Default).launch {
                    val batteryInfo = app.batteryRepository.getBatteryInfoSnapshot()
                    app.glyphRepository.triggerBatteryFlash(batteryInfo.level)
                }
            }
        }

        // Update widgets on battery change
        if (intent.action == Intent.ACTION_BATTERY_CHANGED ||
            intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, NothingBatteryWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 50

            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val views = RemoteViews(context.packageName, R.layout.widget_nothing_battery)

            views.setTextViewText(R.id.widget_battery_pct, "$batteryPct%")
            views.setTextViewText(
                R.id.widget_battery_status,
                if (isCharging) "CHARGING • GLYPH READY" else "NOTHING PHONE • BATTERY"
            )

            // Open app on body click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Flash Glyph on button click
            val flashIntent = Intent(context, NothingBatteryWidgetProvider::class.java).apply {
                action = NothingGlyphConstants.ACTION_FLASH_BATTERY
            }
            val flashPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                flashIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_flash, flashPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
