package com.uniruta.app.device.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.uniruta.app.data.model.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val UPDATE_INTERVAL_MILLIS = 4_000L
private const val MIN_UPDATE_INTERVAL_MILLIS = 2_000L
private const val MAX_LAST_LOCATION_AGE_NANOS = 120_000_000_000L

class FusedLocationTracker(context: Context) : LocationTracker {

    private val appContext = context.applicationContext
    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(appContext)
    }

    override fun readiness(): LocationReadiness = when {
        !hasAnyPermission() -> LocationReadiness.PERMISSION_DENIED
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) != ConnectionResult.SUCCESS ->
            LocationReadiness.SERVICES_UNAVAILABLE
        !isLocationEnabled() -> LocationReadiness.PROVIDER_DISABLED
        else -> LocationReadiness.READY
    }

    @SuppressLint("MissingPermission")
    override fun updates(): Flow<LocationEvent> = callbackFlow {
        if (!hasAnyPermission()) throw SecurityException("Location permission not granted")

        val priority = if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val request = LocationRequest.Builder(priority, UPDATE_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toFix()) }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) trySend(LocationEvent.SignalLost)
            }
        }

        client.lastLocation.addOnSuccessListener { location ->
            if (location != null && location.isRecent()) trySend(location.toFix())
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { error -> close(error) }

        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun hasAnyPermission(): Boolean =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val manager = appContext.getSystemService(LocationManager::class.java) ?: return false
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    private fun Location.isRecent(): Boolean =
        SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos <= MAX_LAST_LOCATION_AGE_NANOS

    private fun Location.toFix(): LocationEvent.Fix = LocationEvent.Fix(
        point = GeoPoint(latitude = latitude, longitude = longitude),
        accuracyMeters = if (hasAccuracy()) accuracy else null
    )
}
