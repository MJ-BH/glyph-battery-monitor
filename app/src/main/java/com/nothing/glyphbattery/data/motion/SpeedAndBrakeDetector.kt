package com.nothing.glyphbattery.data.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

data class MotionData(
    val speedKmh: Float = 0.0f,
    val isBraking: Boolean = false,
    val accelerationMagnitude: Float = 0.0f
)

class SpeedAndBrakeDetector(
    private val context: Context
) {
    private val tag = "SpeedAndBrakeDetector"

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _motionData = MutableStateFlow(MotionData())
    val motionData: StateFlow<MotionData> = _motionData.asStateFlow()

    private var previousSpeed = 0f
    private var lastSpeedCheckTimestamp = 0L

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                // Deceleration along the Y/Z axis indicates braking
                val isBrakingForce = y < -3.5f || z < -4.0f
                if (isBrakingForce != _motionData.value.isBraking) {
                    _motionData.update { it.copy(isBraking = isBrakingForce, accelerationMagnitude = magnitude) }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speedMps = location.speed
            val speedKmh = speedMps * 3.6f

            val now = System.currentTimeMillis()
            val dt = (now - lastSpeedCheckTimestamp) / 1000.0f
            var isDecel = false

            if (dt in 0.5f..3.0f && previousSpeed > 10f) {
                val speedDrop = previousSpeed - speedKmh
                if (speedDrop > 4.5f) { // Significant speed drop = braking
                    isDecel = true
                }
            }

            previousSpeed = speedKmh
            lastSpeedCheckTimestamp = now

            _motionData.update {
                it.copy(
                    speedKmh = speedKmh.coerceAtLeast(0f),
                    isBraking = isDecel || it.isBraking
                )
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun startListening() {
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sensorManager?.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_UI)
        }

        try {
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    2.0f,
                    locationListener
                )
            }
        } catch (_: SecurityException) {
            Log.w(tag, "Location permission not granted. Speed estimation will rely on simulated inputs.")
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(sensorListener)
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (_: Exception) {}
    }

    fun updateManualSpeedAndBrake(speedKmh: Float, isBraking: Boolean) {
        _motionData.update { it.copy(speedKmh = speedKmh, isBraking = isBraking) }
    }
}
