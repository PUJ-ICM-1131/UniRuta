package com.uniruta.app.data.routing

import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.RoutePath

enum class DirectionsError(val message: String, val retryable: Boolean) {
    MISSING_KEY("Falta configurar la clave de OpenRouteService (ORS_API_KEY).", false),
    UNAUTHORIZED("OpenRouteService rechazó la clave configurada.", false),
    RATE_LIMITED("Se alcanzó el límite de solicitudes de OpenRouteService.", true),
    NO_ROUTE("OpenRouteService no encontró una ruta vial entre las paradas.", false),
    NETWORK("No fue posible conectarse con OpenRouteService. Revisa la conexión a internet.", true),
    SERVER("OpenRouteService no pudo procesar la solicitud.", true),
    INVALID_RESPONSE("La respuesta de OpenRouteService no contiene una ruta válida.", true),
    NOT_ENOUGH_POINTS("Se necesitan al menos dos paradas con ubicación.", false)
}

sealed interface DirectionsResult {
    data class Success(val path: RoutePath) : DirectionsResult
    data class Failure(val error: DirectionsError) : DirectionsResult
}

fun interface RouteDirectionsService {
    suspend fun directions(waypoints: List<GeoPoint>): DirectionsResult
}
