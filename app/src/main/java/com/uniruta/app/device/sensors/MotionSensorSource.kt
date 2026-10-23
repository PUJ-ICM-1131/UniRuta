package com.uniruta.app.device.sensors

import com.uniruta.app.data.model.SensorSource
import kotlinx.coroutines.flow.Flow

data class MotionSample(
    val source: SensorSource,
    val x: Float,
    val y: Float,
    val z: Float,
    val timestampNanos: Long
)

interface MotionSensorSource {
    fun isAvailable(source: SensorSource): Boolean
    fun samples(): Flow<MotionSample>
}
