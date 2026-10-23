package com.uniruta.app.data.geo

import com.uniruta.app.data.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
    val fromLatitude = Math.toRadians(from.latitude)
    val toLatitude = Math.toRadians(to.latitude)
    val deltaLatitude = toLatitude - fromLatitude
    val deltaLongitude = Math.toRadians(to.longitude - from.longitude)
    val haversine = sin(deltaLatitude / 2).pow(2) +
        cos(fromLatitude) * cos(toLatitude) * sin(deltaLongitude / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
}

fun List<GeoPoint>.pathLengthMeters(): Double =
    zipWithNext().sumOf { (from, to) -> distanceMeters(from, to) }

fun List<GeoPoint>.pointAtFraction(fraction: Double): GeoPoint? {
    if (isEmpty()) return null
    if (size == 1) return first()
    val target = pathLengthMeters() * fraction.coerceIn(0.0, 1.0)
    var travelled = 0.0
    for ((from, to) in zipWithNext()) {
        val segment = distanceMeters(from, to)
        if (segment > 0.0 && travelled + segment >= target) {
            val ratio = (target - travelled) / segment
            return GeoPoint(
                latitude = from.latitude + (to.latitude - from.latitude) * ratio,
                longitude = from.longitude + (to.longitude - from.longitude) * ratio
            )
        }
        travelled += segment
    }
    return last()
}
