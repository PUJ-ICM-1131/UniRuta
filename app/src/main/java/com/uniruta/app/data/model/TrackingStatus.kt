package com.uniruta.app.data.model

enum class TrackingStatus(val label: String) {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SEARCHING("Esperando datos"),
    PAUSED("En pausa"),
    PERMISSION_DENIED("Sin permiso"),
    DISABLED("Desactivado"),
    UNAVAILABLE("No disponible"),
    ERROR("Error")
}
