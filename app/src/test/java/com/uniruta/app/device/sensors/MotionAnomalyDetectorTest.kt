package com.uniruta.app.device.sensors

import com.uniruta.app.data.model.AnomalyType
import com.uniruta.app.data.model.SensorSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val STEP_NANOS = 20_000_000L

class MotionAnomalyDetectorTest {

    private class Timeline {
        var nanos = 0L

        fun accelerometer(x: Float, y: Float, z: Float): MotionSample =
            MotionSample(SensorSource.ACCELEROMETER, x, y, z, nanos).also { nanos += STEP_NANOS }

        fun gyroscope(x: Float, y: Float, z: Float): MotionSample =
            MotionSample(SensorSource.GYROSCOPE, x, y, z, nanos)
    }

    private fun MotionAnomalyDetector.feed(
        timeline: Timeline,
        seconds: Float,
        accelerometer: Triple<Float, Float, Float>,
        gyroscope: Triple<Float, Float, Float> = Triple(0f, 0f, 0f)
    ): List<MotionDetection> {
        val detections = mutableListOf<MotionDetection>()
        repeat((seconds * 50).toInt()) {
            process(timeline.gyroscope(gyroscope.first, gyroscope.second, gyroscope.third))?.let(detections::add)
            process(timeline.accelerometer(accelerometer.first, accelerometer.second, accelerometer.third))
                ?.let(detections::add)
        }
        return detections
    }

    private val uprightAtRest = Triple(0f, STANDARD_GRAVITY, 0f)

    @Test
    fun stationaryPhoneProducesNoEvents() {
        val detector = MotionAnomalyDetector()

        val detections = detector.feed(Timeline(), seconds = 6f, accelerometer = uprightAtRest)

        assertTrue(detections.isEmpty())
    }

    @Test
    fun brakingWithUprightPhoneIsHardBraking() {
        val detector = MotionAnomalyDetector()
        val timeline = Timeline()
        detector.feed(timeline, seconds = 3f, accelerometer = uprightAtRest)

        val detections = detector.feed(timeline, seconds = 1f, accelerometer = Triple(0f, STANDARD_GRAVITY, 4.5f))

        assertEquals(1, detections.size)
        assertEquals(AnomalyType.HARD_BRAKING, detections.single().type)
        assertTrue(detections.single().magnitude >= 3.5f)
    }

    @Test
    fun forwardAccelerationWithUprightPhoneIsHardAcceleration() {
        val detector = MotionAnomalyDetector()
        val timeline = Timeline()
        detector.feed(timeline, seconds = 3f, accelerometer = uprightAtRest)

        val detections = detector.feed(timeline, seconds = 1f, accelerometer = Triple(0f, STANDARD_GRAVITY, -4f))

        assertEquals(listOf(AnomalyType.HARD_ACCELERATION), detections.map { it.type })
    }

    @Test
    fun brakingWithPhoneLyingFlatIsDetected() {
        val detector = MotionAnomalyDetector()
        val timeline = Timeline()
        val flatAtRest = Triple(0f, 0f, STANDARD_GRAVITY)
        detector.feed(timeline, seconds = 3f, accelerometer = flatAtRest)

        val detections = detector.feed(timeline, seconds = 1f, accelerometer = Triple(0f, -4.5f, STANDARD_GRAVITY))

        assertEquals(listOf(AnomalyType.HARD_BRAKING), detections.map { it.type })
    }

    @Test
    fun rotationAroundGravityIsSharpTurn() {
        val detector = MotionAnomalyDetector()
        val timeline = Timeline()
        detector.feed(timeline, seconds = 3f, accelerometer = uprightAtRest)

        val detections = detector.feed(
            timeline,
            seconds = 1f,
            accelerometer = uprightAtRest,
            gyroscope = Triple(0f, 1.0f, 0f)
        )

        assertEquals(1, detections.size)
        assertEquals(AnomalyType.SHARP_TURN, detections.single().type)
        assertTrue(detections.single().magnitude >= 35f)
    }

    @Test
    fun handlingThePhoneDoesNotCountAsTurn() {
        val detector = MotionAnomalyDetector()
        val timeline = Timeline()
        detector.feed(timeline, seconds = 3f, accelerometer = uprightAtRest)

        val detections = detector.feed(
            timeline,
            seconds = 1f,
            accelerometer = uprightAtRest,
            gyroscope = Triple(3f, 0.9f, 0f)
        )

        assertTrue(detections.isEmpty())
    }

    @Test
    fun cooldownAvoidsDuplicatedEvents() {
        val detector = MotionAnomalyDetector(MotionThresholds(cooldownMillis = 3_000L))
        val timeline = Timeline()
        detector.feed(timeline, seconds = 3f, accelerometer = uprightAtRest)

        val detections = detector.feed(timeline, seconds = 4f, accelerometer = Triple(0f, STANDARD_GRAVITY, 4.5f))

        assertEquals(2, detections.size)
        assertTrue(detections.all { it.type == AnomalyType.HARD_BRAKING })
    }

    @Test
    fun eventsAreIgnoredDuringWarmUp() {
        val detector = MotionAnomalyDetector()

        val detections = detector.feed(
            Timeline(),
            seconds = 1.5f,
            accelerometer = uprightAtRest,
            gyroscope = Triple(0f, 1.5f, 0f)
        )

        assertTrue(detections.isEmpty())
    }
}
