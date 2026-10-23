package com.uniruta.app.data.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
            !latitude.isNaN() && !longitude.isNaN()
}
