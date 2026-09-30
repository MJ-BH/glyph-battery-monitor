package com.nothing.glyphbattery.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nothing.glyphbattery.R
import com.nothing.glyphbattery.domain.model.GlyphAnimationMode
import com.nothing.glyphbattery.domain.repository.BatteryRepository
import com.nothing.glyphbattery.domain.repository.GlyphRepository
import com.nothing.glyphbattery.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Foreground Service that maintains Glyph charging feedback & status.
 */
class GlyphBatteryForegroundService : Service(), KoinComponent {

    private val batteryRepo: BatteryRepository by inject()
    private val glyphRepo: GlyphRepository by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var syncJob: Job? = null

    companion object {
        const val CHANNEL_ID = "glyph_battery_service_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "ACTION_START_GLYPH_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_GLYPH_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, GlyphBatteryForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GlyphBatteryForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification("Glyph Charging Monitor Active", "Synchronizing Glyph lights with battery")
        startForeground(NOTIFICATION_ID, notification)

        startObservingBattery()

        return START_STICKY
    }

    private fun startObservingBattery() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                batteryRepo.observeBatteryInfo(),
                glyphRepo.glyphState,
                glyphRepo.observeFaceDown()
            ) { batteryInfo, glyphState, isFaceDown ->
                Triple(batteryInfo, glyphState, isFaceDown)
            }.collectLatest { (batteryInfo, state, isFaceDown) ->
                if (batteryInfo.isCharging) {
                    if (state.flipToGlyphCharging && isFaceDown) {
                        // Phone is turned face-down on table & charging -> Always show battery progress!
                        glyphRepo.openSession()
                        glyphRepo.displayProgress(batteryInfo.level)
                    } else if (state.syncWithCharging) {
                        glyphRepo.openSession()
                        glyphRepo.setAnimationMode(GlyphAnimationMode.BREATHING_CHARGING)
                    } else {
                        glyphRepo.turnOff()
                    }
                } else {
                    glyphRepo.closeSession()
                }

                val statusText = if (batteryInfo.isCharging) {
                    if (isFaceDown) "Charging: ${batteryInfo.level}% (Face-Down Glyph Active)"
                    else "Charging: ${batteryInfo.level}% (Glyph Sync Active)"
                } else {
                    "Battery: ${batteryInfo.level}%"
                }

                updateNotification(statusText)
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_battery_bolt)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification("Nothing Glyph Battery", content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Glyph Battery Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live battery charging Glyph synchronization status"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        syncJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
