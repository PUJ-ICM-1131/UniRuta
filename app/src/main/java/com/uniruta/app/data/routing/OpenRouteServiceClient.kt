package com.uniruta.app.data.routing

import com.uniruta.app.config.ApiKeys
import com.uniruta.app.data.model.GeoPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

private const val DEFAULT_BASE_URL = "https://api.openrouteservice.org"
private const val DIRECTIONS_PATH = "/v2/directions/driving-car/geojson"
private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 20_000
private const val SNAP_RADIUS_METERS = 1_000

class OpenRouteServiceClient(
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RouteDirectionsService {

    override suspend fun directions(waypoints: List<GeoPoint>): DirectionsResult {
        if (!ApiKeys.isConfigured(apiKey)) return DirectionsResult.Failure(DirectionsError.MISSING_KEY)
        if (waypoints.size < 2) return DirectionsResult.Failure(DirectionsError.NOT_ENOUGH_POINTS)
        return withContext(dispatcher) { request(waypoints) }
    }

    private fun request(waypoints: List<GeoPoint>): DirectionsResult {
        val connection = try {
            URI.create(baseUrl + DIRECTIONS_PATH).toURL().openConnection() as HttpURLConnection
        } catch (error: IOException) {
            return DirectionsResult.Failure(DirectionsError.NETWORK)
        }

        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.doOutput = true
            connection.setRequestProperty("Authorization", apiKey.trim())
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/geo+json, application/json")

            val body = OpenRouteServiceParser.requestBody(waypoints, SNAP_RADIUS_METERS)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            if (status in 200..299) {
                val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                OpenRouteServiceParser.parseRoute(response)
            } else {
                val error = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                DirectionsResult.Failure(OpenRouteServiceParser.errorFor(status, error))
            }
        } catch (error: IOException) {
            DirectionsResult.Failure(DirectionsError.NETWORK)
        } finally {
            connection.disconnect()
        }
    }
}
