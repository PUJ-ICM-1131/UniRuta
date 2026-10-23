package com.uniruta.app.device.sensors

import com.uniruta.app.data.model.AnomalyType
import com.uniruta.app.data.model.SensorSource
import kotlin.math.abs
import kotlin.math.sqrt

const val STANDARD_GRAVITY = 9.80665f

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val NANOS_PER_MILLI = 1_000_000L
private const val MAX_STEP_SECONDS = 0.5f
private const val MIN_HORIZONTAL_PROJECTION = 0.2f

data class MotionThresholds(
    val hardAccelerationMps2: Float = 3.0f,
    val hardBrakingMps2: Float = 3.5f,
    val sharpTurnDegreesPerSecond: Float = 35f,
    val cooldownMillis: Long = 3_000L,
    val warmUpMillis: Long = 2_000L,
    val gravityTimeConstantSeconds: Float = 1.0f,
    val smoothingTimeConstantSeconds: Float = 0.15f,
    val gravityToleranceMps2: Float = 0.35f,
    val handlingRateRadPerSecond: Float = 2.0f,
    val handlingQuietMillis: Long = 1_000L
)

data class MotionDetection(
    val type: AnomalyType,
    val magnitude: Float
)

class MotionAnomalyDetector(
    private val thresholds: MotionThresholds = MotionThresholds()
) {

    private var gravity: FloatArray? = null
    private var startNanos = 0L
    private var lastAccelerometerNanos = 0L
    private var lastGyroscopeNanos: Long? = null
    private var longitudinal = 0f
    private var yawRate = 0f
    private var handlingUntilNanos: Long? = null
    private val lastDetectionNanos = mutableMapOf<AnomalyType, Long>()

    fun reset() {
        gravity = null
        startNanos = 0L
        lastAccelerometerNanos = 0L
        lastGyroscopeNanos = null
        longitudinal = 0f
        yawRate = 0f
        handlingUntilNanos = null
        lastDetectionNanos.clear()
    }

    fun process(sample: MotionSample): MotionDetection? = when (sample.source) {
        SensorSource.ACCELEROMETER -> processAcceleration(sample)
        SensorSource.GYROSCOPE -> processRotation(sample)
    }

    private fun processAcceleration(sample: MotionSample): MotionDetection? {
        val raw = floatArrayOf(sample.x, sample.y, sample.z)
        val estimate = gravity
        if (estimate == null) {
            gravity = raw
            startNanos = sample.timestampNanos
            lastAccelerometerNanos = sample.timestampNanos
            return null
        }

        val step = stepSeconds(lastAccelerometerNanos, sample.timestampNanos)
        lastAccelerometerNanos = sample.timestampNanos

        if (abs(norm(raw) - STANDARD_GRAVITY) <= thresholds.gravityToleranceMps2) {
            val alpha = filterAlpha(step, thresholds.gravityTimeConstantSeconds)
            for (axis in 0..2) estimate[axis] += alpha * (raw[axis] - estimate[axis])
        }

        val up = normalized(estimate) ?: return null
        val forward = horizontalForward(up) ?: return null
        val linear = FloatArray(3) { raw[it] - estimate[it] }
        val value = dot(linear, forward)
        longitudinal += filterAlpha(step, thresholds.smoothingTimeConstantSeconds) * (value - longitudinal)

        if (!isArmed(sample.timestampNanos)) return null
        return when {
            longitudinal >= thresholds.hardAccelerationMps2 ->
                emit(AnomalyType.HARD_ACCELERATION, longitudinal, sample.timestampNanos)
            longitudinal <= -thresholds.hardBrakingMps2 ->
                emit(AnomalyType.HARD_BRAKING, -longitudinal, sample.timestampNanos)
            else -> null
        }
    }

    private fun processRotation(sample: MotionSample): MotionDetection? {
        val estimate = gravity ?: return null
        val up = normalized(estimate) ?: return null
        val previous = lastGyroscopeNanos
        lastGyroscopeNanos = sample.timestampNanos
        val step = if (previous == null) 0f else stepSeconds(previous, sample.timestampNanos)

        val rotation = floatArrayOf(sample.x, sample.y, sample.z)
        val yaw = dot(rotation, up)
        val tilt = norm(FloatArray(3) { rotation[it] - yaw * up[it] })
        if (tilt >= thresholds.handlingRateRadPerSecond) {
            handlingUntilNanos = sample.timestampNanos + thresholds.handlingQuietMillis * NANOS_PER_MILLI
        }
        yawRate += filterAlpha(step, thresholds.smoothingTimeConstantSeconds) * (yaw - yawRate)

        if (!isArmed(sample.timestampNanos)) return null
        val degrees = abs(Math.toDegrees(yawRate.toDouble())).toFloat()
        return if (degrees >= thresholds.sharpTurnDegreesPerSecond) {
            emit(AnomalyType.SHARP_TURN, degrees, sample.timestampNanos)
        } else {
            null
        }
    }

    private fun isArmed(timestampNanos: Long): Boolean {
        if (timestampNanos - startNanos < thresholds.warmUpMillis * NANOS_PER_MILLI) return false
        val handlingUntil = handlingUntilNanos
        return handlingUntil == null || timestampNanos >= handlingUntil
    }

    private fun emit(type: AnomalyType, magnitude: Float, timestampNanos: Long): MotionDetection? {
        val last = lastDetectionNanos[type]
        if (last != null && timestampNanos - last < thresholds.cooldownMillis * NANOS_PER_MILLI) return null
        lastDetectionNanos[type] = timestampNanos
        return MotionDetection(type = type, magnitude = magnitude)
    }

    private fun horizontalForward(up: FloatArray): FloatArray? {
        val reference = floatArrayOf(0f, 1f, -1f)
        val along = dot(reference, up)
        val projection = FloatArray(3) { reference[it] - along * up[it] }
        val length = norm(projection)
        if (length < MIN_HORIZONTAL_PROJECTION) return null
        return FloatArray(3) { projection[it] / length }
    }

    private fun stepSeconds(fromNanos: Long, toNanos: Long): Float =
        ((toNanos - fromNanos) / NANOS_PER_SECOND).coerceIn(0f, MAX_STEP_SECONDS)

    private fun filterAlpha(stepSeconds: Float, timeConstantSeconds: Float): Float =
        if (stepSeconds <= 0f) 0f else stepSeconds / (timeConstantSeconds + stepSeconds)

    private fun normalized(vector: FloatArray): FloatArray? {
        val length = norm(vector)
        if (length < 1f) return null
        return FloatArray(3) { vector[it] / length }
    }

    private fun dot(a: FloatArray, b: FloatArray): Float = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]

    private fun norm(vector: FloatArray): Float = sqrt(dot(vector, vector))
}
