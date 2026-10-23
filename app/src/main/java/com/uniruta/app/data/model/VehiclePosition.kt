package com.uniruta.app.data.model

import java.time.LocalTime

enum class PositionSource(val label: String) {
    DEVICE_GPS("GPS del dispositivo"),
    DEMO("Posición simulada · demostración")
}

data class VehiclePosition(
    val point: GeoPoint,
    val source: PositionSource,
    val updatedAt: LocalTime,
    val accuracyMeters: Float? = null
)
