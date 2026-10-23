package com.uniruta.app.data.model

enum class RoutePathSource(val label: String) {
    OPEN_ROUTE_SERVICE("Ruta vial calculada con OpenRouteService"),
    APPROXIMATE("Trazado aproximado entre paradas · demostración")
}

data class RoutePath(
    val points: List<GeoPoint>,
    val distanceMeters: Double?,
    val durationSeconds: Double?,
    val source: RoutePathSource
) {
    val isCalculated: Boolean
        get() = source == RoutePathSource.OPEN_ROUTE_SERVICE
}
