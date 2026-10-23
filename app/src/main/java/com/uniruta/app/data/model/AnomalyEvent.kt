package com.uniruta.app.data.model

import java.time.LocalTime

enum class SensorSource(val label: String, val unit: String) {
    ACCELEROMETER("Acelerómetro", "m/s²"),
    GYROSCOPE("Giroscopio", "°/s")
}

enum class AnomalyType(val label: String, val source: SensorSource) {
    HARD_BRAKING("Frenada brusca", SensorSource.ACCELEROMETER),
    HARD_ACCELERATION("Aceleración brusca", SensorSource.ACCELEROMETER),
    SHARP_TURN("Giro pronunciado", SensorSource.GYROSCOPE)
}

enum class EventOrigin(val label: String) {
    SENSOR("Lectura del sensor"),
    DEMO("Demostración")
}

data class AnomalyEvent(
    val id: String,
    val tripId: String,
    val type: AnomalyType,
    val time: LocalTime,
    val origin: EventOrigin,
    val magnitude: Float? = null
)
