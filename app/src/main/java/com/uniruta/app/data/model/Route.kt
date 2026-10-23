package com.uniruta.app.data.model

data class Stop(
    val id: String,
    val name: String,
    val order: Int,
    val location: GeoPoint? = null
)

data class Route(
    val id: String,
    val name: String,
    val origin: String,
    val destination: String,
    val stops: List<Stop>,
    val active: Boolean = true
) {
    val orderedStops: List<Stop>
        get() = stops.sortedBy { it.order }

    val locatedStops: List<Stop>
        get() = orderedStops.filter { it.location?.isValid == true }

    val originLocation: GeoPoint?
        get() = orderedStops.firstOrNull()?.location

    val destinationLocation: GeoPoint?
        get() = orderedStops.lastOrNull()?.location

    val isMappable: Boolean
        get() = locatedStops.size >= 2
}
