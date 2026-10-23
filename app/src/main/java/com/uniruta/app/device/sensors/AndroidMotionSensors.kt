package com.uniruta.app.device.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.uniruta.app.data.model.SensorSource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

private const val SAMPLE_BUFFER = 128

class AndroidMotionSensors(
    context: Context,
    private val samplingPeriod: Int = SensorManager.SENSOR_DELAY_GAME
) : MotionSensorSource {

    private val sensorManager: SensorManager? =
        context.applicationContext.getSystemService(SensorManager::class.java)

    override fun isAvailable(source: SensorSource): Boolean = sensorFor(source) != null

    override fun samples(): Flow<MotionSample> = callbackFlow {
        val manager = sensorManager ?: throw IllegalStateException("SensorManager unavailable")

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val source = when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> SensorSource.ACCELEROMETER
                    Sensor.TYPE_GYROSCOPE -> SensorSource.GYROSCOPE
                    else -> return
                }
                trySend(
                    MotionSample(
                        source = source,
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        timestampNanos = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        SensorSource.entries.mapNotNull(::sensorFor).forEach { sensor ->
            manager.registerListener(listener, sensor, samplingPeriod)
        }

        awaitClose { manager.unregisterListener(listener) }
    }.buffer(capacity = SAMPLE_BUFFER, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private fun sensorFor(source: SensorSource): Sensor? = sensorManager?.getDefaultSensor(
        when (source) {
            SensorSource.ACCELEROMETER -> Sensor.TYPE_ACCELEROMETER
            SensorSource.GYROSCOPE -> Sensor.TYPE_GYROSCOPE
        }
    )
}
