package com.uniruta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.uniruta.app.config.ApiKeys
import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.PositionSource
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.RoutePath
import com.uniruta.app.data.model.Stop
import com.uniruta.app.data.model.VehiclePosition
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.ui.theme.UniRutaIcons

private const val SINGLE_POINT_ZOOM = 15f
private const val INITIAL_ZOOM = 11f

private val mapTopInset = 28.dp

private val mapUiSettings = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = true
)

@Composable
fun RouteMapCard(
    route: Route,
    pathState: RoutePathState?,
    modifier: Modifier = Modifier,
    vehicle: VehiclePosition? = null,
    vehicleTitle: String = "Vehículo",
    onRetry: (() -> Unit)? = null,
    onMapInteraction: (Boolean) -> Unit = {}
) {
    val path = (pathState as? RoutePathState.Ready)?.path
    val missingStops = route.stops.size - route.locatedStops.size

    UniRutaCard(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                when {
                    !ApiKeys.mapsConfigured -> MapMessage(
                        "Mapa no disponible: falta configurar MAPS_API_KEY en local.properties."
                    )
                    !route.isMappable -> MapMessage(
                        "Esta ruta todavía no tiene paradas con ubicación geográfica."
                    )
                    else -> RouteGoogleMap(
                        stops = route.locatedStops,
                        path = path,
                        vehicle = vehicle,
                        vehicleTitle = vehicleTitle,
                        onMapInteraction = onMapInteraction
                    )
                }
                PathChip(
                    pathState = pathState,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MapNote(text = route.origin)
                MapNote(text = if (route.stops.size == 1) "1 parada" else "${route.stops.size} paradas")
                MapNote(text = route.destination.substringAfterLast(' '))
            }

            Column(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (pathState) {
                    RoutePathState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        MapNote(
                            text = "Calculando la ruta vial con OpenRouteService…",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    is RoutePathState.Ready -> {
                        MapNote(text = pathState.path.source.label)
                        pathState.notice?.let { MapNote(text = it) }
                        if (pathState.retryable && onRetry != null) {
                            LinkButton(text = "Reintentar cálculo de ruta", onClick = onRetry, icon = UniRutaIcons.Refresh)
                        }
                    }
                    is RoutePathState.Unavailable -> MapNote(text = pathState.reason)
                    null -> Unit
                }
                if (route.isMappable) {
                    MapNote(text = "Ubicación de paradas: referencia aproximada tomada de OpenStreetMap.")
                }
                if (missingStops > 0) {
                    MapNote(
                        text = if (missingStops == 1) {
                            "1 parada no tiene ubicación y no aparece en el mapa."
                        } else {
                            "$missingStops paradas no tienen ubicación y no aparecen en el mapa."
                        }
                    )
                }
                vehicle?.let { position ->
                    MapNote(
                        text = when (position.source) {
                            PositionSource.DEMO -> "$vehicleTitle: posición simulada sobre el trayecto, no es una ubicación transmitida."
                            PositionSource.DEVICE_GPS -> "$vehicleTitle: ubicación real reportada por el GPS de este dispositivo."
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteGoogleMap(
    stops: List<Stop>,
    path: RoutePath?,
    vehicle: VehiclePosition?,
    vehicleTitle: String,
    onMapInteraction: (Boolean) -> Unit
) {
    val firstPoint = stops.firstNotNullOf { it.location }.toLatLng()
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstPoint, INITIAL_ZOOM)
    }
    val focusPoints = remember(stops, path) {
        (path?.points ?: stops.mapNotNull { it.location }).map { it.toLatLng() }
    }
    val pathPoints = remember(path) { path?.points.orEmpty().map { it.toLatLng() } }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val paddingPx = with(density) { 36.dp.roundToPx() }
    val topInsetPx = with(density) { mapTopInset.roundToPx() }
    val interactionCallback by rememberUpdatedState(onMapInteraction)

    LaunchedEffect(mapSize, focusPoints) {
        if (mapSize == IntSize.Zero || focusPoints.isEmpty()) return@LaunchedEffect
        val update = if (focusPoints.size == 1) {
            CameraUpdateFactory.newLatLngZoom(focusPoints.first(), SINGLE_POINT_ZOOM)
        } else {
            val bounds = LatLngBounds.builder().apply { focusPoints.forEach(::include) }.build()
            CameraUpdateFactory.newLatLngBounds(bounds, mapSize.width, mapSize.height - topInsetPx, paddingPx)
        }
        cameraState.move(update)
    }

    val interacting = cameraState.isMoving &&
        cameraState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
    LaunchedEffect(interacting) { interactionCallback(interacting) }

    val lineColor = MaterialTheme.colorScheme.primary
    val accuracyColor = MaterialTheme.colorScheme.tertiary

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { mapSize = it },
        cameraPositionState = cameraState,
        contentPadding = PaddingValues(top = mapTopInset),
        uiSettings = mapUiSettings,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM
    ) {
        if (pathPoints.size >= 2) {
            Polyline(
                points = pathPoints,
                color = lineColor,
                width = 12f,
                jointType = JointType.ROUND,
                startCap = RoundCap(),
                endCap = RoundCap(),
                pattern = if (path?.isCalculated == true) null else listOf(Dash(30f), Gap(18f))
            )
        }

        stops.forEachIndexed { index, stop ->
            val location = stop.location ?: return@forEachIndexed
            key(stop.id) {
                Marker(
                    state = rememberUpdatedMarkerState(position = location.toLatLng()),
                    title = "${stop.order}. ${stop.name}",
                    snippet = when (index) {
                        0 -> "Origen"
                        stops.lastIndex -> "Destino"
                        else -> "Parada intermedia"
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(
                        when (index) {
                            0 -> BitmapDescriptorFactory.HUE_GREEN
                            stops.lastIndex -> BitmapDescriptorFactory.HUE_RED
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }
                    )
                )
            }
        }

        vehicle?.let { position ->
            val center = position.point.toLatLng()
            position.accuracyMeters?.let { accuracy ->
                Circle(
                    center = center,
                    radius = accuracy.toDouble(),
                    fillColor = accuracyColor.copy(alpha = 0.15f),
                    strokeColor = accuracyColor.copy(alpha = 0.6f),
                    strokeWidth = 2f
                )
            }
            Marker(
                state = rememberUpdatedMarkerState(position = center),
                title = vehicleTitle,
                snippet = position.source.label,
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (position.source == PositionSource.DEMO) {
                        BitmapDescriptorFactory.HUE_ORANGE
                    } else {
                        BitmapDescriptorFactory.HUE_VIOLET
                    }
                ),
                zIndex = 2f
            )
        }
    }
}

@Composable
private fun PathChip(pathState: RoutePathState?, modifier: Modifier = Modifier) {
    when (pathState) {
        RoutePathState.Loading -> StatusChip(label = "Calculando", tone = StatusTone.NEUTRAL, modifier = modifier)
        is RoutePathState.Ready -> if (pathState.path.isCalculated) {
            StatusChip(label = "Ruta calculada", tone = StatusTone.POSITIVE, modifier = modifier)
        } else {
            StatusChip(label = "Trazado aproximado", tone = StatusTone.WARNING, modifier = modifier)
        }
        is RoutePathState.Unavailable -> StatusChip(label = "Sin trazado", tone = StatusTone.NEUTRAL, modifier = modifier)
        null -> Unit
    }
}

@Composable
private fun MapMessage(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = UniRutaIcons.Map,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun MapNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

private fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)
