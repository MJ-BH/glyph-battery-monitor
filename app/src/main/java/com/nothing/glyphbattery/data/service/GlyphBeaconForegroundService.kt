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
import com.nothing.glyphbattery.GlyphBatteryApp
import com.nothing.glyphbattery.R
import com.nothing.glyphbattery.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GlyphBeaconForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var syncJob: Job? = null

    companion object {
        const val CHANNEL_ID = "glyph_beacon_service_channel"
        const val NOTIFICATION_ID = 202

        const val ACTION_START_BEACON = "ACTION_START_BEACON"
        const val ACTION_STOP_BEACON = "ACTION_STOP_BEACON"

        fun start(context: Context) {
            val intent = Intent(context, GlyphBeaconForegroundService::class.java).apply {
                action = ACTION_START_BEACON
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GlyphBeaconForegroundService::class.java).apply {
                action = ACTION_STOP_BEACON
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_BEACON) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification("Glyph Beacon & CMF Companion Active", "Strobe & Bio-Pulse active")
        startForeground(NOTIFICATION_ID, notification)

        startObservingSession()

        return START_STICKY
    }

    private fun startObservingSession() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            val app = application as? GlyphBatteryApp ?: return@launch
            val beaconRepo = app.beaconRepository
            val cmfRepo = app.cmfWatchRepository

            beaconRepo.sessionState.collectLatest { session ->
                val cmfState = cmfRepo.watchMetrics.value
                val statusText = "Mode: ${session.activeMode.title} | Speed: ${session.speedKmh.toInt()} km/h | HR: ${cmfState.heartRateBpm} BPM"
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
        manager?.notify(NOTIFICATION_ID, buildNotification("Nothing Glyph Beacon", content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Glyph Beacon & CMF Companion",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live bike beacon and CMF Watch heart rate sync status"
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
