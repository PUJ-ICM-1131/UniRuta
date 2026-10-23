package com.uniruta.app.data.model

import java.time.LocalTime

enum class IncidentType(val label: String) {
    TRAFFIC("Tráfico"),
    VEHICLE("Vehículo"),
    ROAD("Vía"),
    ANOMALY("Movimiento anómalo"),
    OTHER("Otro")
}

data class Incident(
    val id: String,
    val tripId: String,
    val type: IncidentType,
    val description: String,
    val evidenceName: String?,
    val reportedAt: LocalTime,
    val evidencePath: String? = null
)
