package com.uniruta.app.device.location

import com.uniruta.app.data.model.GeoPoint
import kotlinx.coroutines.flow.Flow

enum class LocationReadiness {
    READY,
    PERMISSION_DENIED,
    PROVIDER_DISABLED,
    SERVICES_UNAVAILABLE
}

sealed interface LocationEvent {
    data class Fix(
        val point: GeoPoint,
        val accuracyMeters: Float?
    ) : LocationEvent

    data object SignalLost : LocationEvent
}

interface LocationTracker {
    fun readiness(): LocationReadiness
    fun updates(): Flow<LocationEvent>
}
