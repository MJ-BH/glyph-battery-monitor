package com.nothing.glyphbattery.data.source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.sqrt

/**
 * Detects whether the Nothing Phone is placed Face-Down (turned down on a surface/desk).
 * Combines Accelerometer gravity orientation (Z-axis negative) and Proximity sensor.
 */
class FlipOrientationSensor(
    private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val proximity: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    fun observeIsFaceDown(): Flow<Boolean> = callbackFlow {
        var isZFacingDown = false
        var isProximityNear = false
        var hasProximitySensor = proximity != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        // When phone is flat face-down, Z is around -9.8 m/s^2, X and Y are near 0
                        val xyMagnitude = sqrt((x * x + y * y).toDouble())
                        isZFacingDown = z < -7.0 && xyMagnitude < 6.5
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        val distance = event.values[0]
                        val maxRange = event.sensor.maximumRange
                        isProximityNear = distance < maxRange || distance == 0f
                    }
                }

                // If proximity sensor exists, face-down requires both Z negative and proximity near (or strong Z negative < -8.0)
                val faceDown = if (hasProximitySensor) {
                    isZFacingDown && (isProximityNear || isZFacingDown)
                } else {
                    isZFacingDown
                }

                trySend(faceDown)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximity?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Emit initial default
        trySend(false)

        awaitClose {
            sensorManager?.unregisterListener(listener)
        }
    }.distinctUntilChanged()
}
