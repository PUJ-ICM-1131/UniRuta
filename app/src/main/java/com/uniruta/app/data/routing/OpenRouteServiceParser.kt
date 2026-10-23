package com.uniruta.app.data.routing

import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.RoutePath
import com.uniruta.app.data.model.RoutePathSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val routeNotFoundCodes = setOf(2004, 2009, 2010)

object OpenRouteServiceParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun requestBody(waypoints: List<GeoPoint>, snapRadiusMeters: Int): String =
        json.encodeToString(
            OrsDirectionsRequest.serializer(),
            OrsDirectionsRequest(
                coordinates = waypoints.map { listOf(it.longitude, it.latitude) },
                radiuses = List(waypoints.size) { snapRadiusMeters },
                instructions = false
            )
        )

    fun parseRoute(body: String): DirectionsResult {
        val collection = try {
            json.decodeFromString(OrsFeatureCollection.serializer(), body)
        } catch (error: SerializationException) {
            return DirectionsResult.Failure(DirectionsError.INVALID_RESPONSE)
        } catch (error: IllegalArgumentException) {
            return DirectionsResult.Failure(DirectionsError.INVALID_RESPONSE)
        }

        val feature = collection.features.firstOrNull()
            ?: return DirectionsResult.Failure(DirectionsError.NO_ROUTE)

        val points = feature.geometry?.coordinates.orEmpty().mapNotNull { pair ->
            if (pair.size < 2) null else GeoPoint(latitude = pair[1], longitude = pair[0]).takeIf { it.isValid }
        }
        if (points.size < 2) return DirectionsResult.Failure(DirectionsError.INVALID_RESPONSE)

        val summary = feature.properties?.summary
        return DirectionsResult.Success(
            RoutePath(
                points = points,
                distanceMeters = summary?.distance,
                durationSeconds = summary?.duration,
                source = RoutePathSource.OPEN_ROUTE_SERVICE
            )
        )
    }

    fun errorFor(statusCode: Int, body: String?): DirectionsError {
        val code = body?.let(::errorCode)
        return when {
            statusCode == 401 || statusCode == 403 -> DirectionsError.UNAUTHORIZED
            statusCode == 429 -> DirectionsError.RATE_LIMITED
            code in routeNotFoundCodes || statusCode == 404 -> DirectionsError.NO_ROUTE
            else -> DirectionsError.SERVER
        }
    }

    private fun errorCode(body: String): Int? = try {
        val error = json.parseToJsonElement(body).jsonObject["error"]
        (error as? JsonObject)?.get("code")?.jsonPrimitive?.intOrNull
    } catch (error: SerializationException) {
        null
    } catch (error: IllegalArgumentException) {
        null
    }
}

@Serializable
private data class OrsDirectionsRequest(
    val coordinates: List<List<Double>>,
    val radiuses: List<Int>,
    val instructions: Boolean
)

@Serializable
private data class OrsFeatureCollection(
    val features: List<OrsFeature> = emptyList()
)

@Serializable
private data class OrsFeature(
    val geometry: OrsGeometry? = null,
    val properties: OrsProperties? = null
)

@Serializable
private data class OrsGeometry(
    val type: String? = null,
    val coordinates: List<List<Double>> = emptyList()
)

@Serializable
private data class OrsProperties(
    val summary: OrsSummary? = null
)

@Serializable
private data class OrsSummary(
    val distance: Double? = null,
    val duration: Double? = null
)
