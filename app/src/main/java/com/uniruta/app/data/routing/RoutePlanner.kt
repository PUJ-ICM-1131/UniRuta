package com.uniruta.app.data.routing

import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.RoutePath
import com.uniruta.app.data.model.RoutePathSource
import java.util.concurrent.ConcurrentHashMap

sealed interface RoutePathState {
    data object Loading : RoutePathState

    data class Ready(
        val path: RoutePath,
        val notice: String? = null,
        val retryable: Boolean = false
    ) : RoutePathState

    data class Unavailable(val reason: String) : RoutePathState
}

class RoutePlanner(private val directions: RouteDirectionsService) {

    private val calculated = ConcurrentHashMap<String, RoutePath>()

    suspend fun pathFor(route: Route): RoutePathState {
        val waypoints = route.locatedStops.mapNotNull { it.location }
        if (waypoints.size < 2) {
            return RoutePathState.Unavailable(
                "Esta ruta todavía no tiene al menos dos paradas con ubicación geográfica."
            )
        }

        val key = cacheKey(route.id, waypoints)
        calculated[key]?.let { return RoutePathState.Ready(it) }

        return when (val result = directions.directions(waypoints)) {
            is DirectionsResult.Success -> {
                calculated[key] = result.path
                RoutePathState.Ready(result.path)
            }
            is DirectionsResult.Failure -> RoutePathState.Ready(
                path = RoutePath(
                    points = waypoints,
                    distanceMeters = null,
                    durationSeconds = null,
                    source = RoutePathSource.APPROXIMATE
                ),
                notice = "${result.error.message} Se muestra un trazado aproximado entre paradas, " +
                    "que no corresponde a la ruta vial.",
                retryable = result.error.retryable
            )
        }
    }

    private fun cacheKey(routeId: String, waypoints: List<GeoPoint>): String =
        routeId + waypoints.joinToString(separator = "|", prefix = "#") {
            "${it.latitude},${it.longitude}"
        }
}
