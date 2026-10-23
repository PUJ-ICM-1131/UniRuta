package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.ui.components.InfoGrid
import com.uniruta.app.ui.components.InfoItem
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.RouteMapCard
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.StopTimeline
import com.uniruta.app.ui.components.TripHeading
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asDistanceText
import com.uniruta.app.ui.format.asDurationText
import com.uniruta.app.ui.format.asLongText
import com.uniruta.app.ui.format.asMetersDistanceText
import com.uniruta.app.ui.format.asSecondsDurationText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun DriverTripDetailScreen(
    trip: Trip,
    passengerCount: Int,
    startBlockedReason: String?,
    pathState: RoutePathState?,
    onRetryRoute: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenActiveTrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var mapInteracting by remember { mutableStateOf(false) }
    val calculatedPath = (pathState as? RoutePathState.Ready)?.path?.takeIf { it.isCalculated }

    UniRutaScreen(
        title = "Detalle del recorrido",
        onBack = onBack,
        modifier = modifier,
        scrollEnabled = !mapInteracting,
        bottomBar = {
            when (trip.status) {
                TripStatus.IN_PROGRESS -> PrimaryButton(
                    text = "Ir al recorrido en curso",
                    onClick = onOpenActiveTrip,
                    icon = UniRutaIcons.Activity
                )
                TripStatus.SCHEDULED -> {
                    if (startBlockedReason != null) {
                        Text(
                            text = startBlockedReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PrimaryButton(
                        text = "Iniciar recorrido",
                        onClick = { confirmVisible = true },
                        enabled = startBlockedReason == null,
                        icon = UniRutaIcons.Play
                    )
                }
                else -> Text(
                    text = "Este recorrido ya no admite acciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) {
        TripHeading(
            title = trip.route.name,
            subtitle = "Hacia ${trip.route.destination}",
            chipLabel = trip.status.label,
            chipTone = trip.status.tone()
        )

        SectionTitle(text = "Vista del recorrido")
        RouteMapCard(
            route = trip.route,
            pathState = pathState,
            onRetry = onRetryRoute,
            onMapInteraction = { mapInteracting = it }
        )

        SectionTitle(text = "Información del recorrido", modifier = Modifier.padding(top = 8.dp))
        InfoGrid(
            items = listOf(
                InfoItem(UniRutaIcons.Calendar, "Fecha", trip.date.asLongText()),
                InfoItem(UniRutaIcons.Clock, "Hora de salida", trip.departureTime.asText()),
                InfoItem(UniRutaIcons.Truck, "Vehículo", trip.vehiclePlate),
                InfoItem(UniRutaIcons.Users, "Capacidad", "${trip.totalSeats} pasajeros"),
                InfoItem(UniRutaIcons.CalendarCheck, "Reservas", if (passengerCount == 1) "1 reserva" else "$passengerCount reservas"),
                InfoItem(UniRutaIcons.MapPin, "Paradas", "${trip.route.stops.size} paradas"),
                InfoItem(
                    UniRutaIcons.Timer,
                    if (calculatedPath?.durationSeconds != null) "Duración calculada" else "Duración estimada",
                    calculatedPath?.durationSeconds?.asSecondsDurationText()
                        ?: trip.estimatedDurationMinutes.asDurationText()
                ),
                InfoItem(
                    UniRutaIcons.ArrowsHorizontal,
                    if (calculatedPath?.distanceMeters != null) "Distancia calculada" else "Distancia estimada",
                    calculatedPath?.distanceMeters?.asMetersDistanceText()
                        ?: trip.estimatedDistanceKm.asDistanceText()
                )
            )
        )

        SectionTitle(text = "Paradas", modifier = Modifier.padding(top = 8.dp))
        StopTimeline(stops = trip.route.orderedStops.map { it.name })

        if (trip.status == TripStatus.SCHEDULED) {
            NoticeBanner(
                text = "Al iniciar se activará el seguimiento de ubicación y el monitoreo del recorrido mientras UniRuta esté abierta.",
                icon = UniRutaIcons.Info,
                tone = StatusTone.INFO
            )
        }
    }

    if (confirmVisible) {
        AlertDialog(
            onDismissRequest = { confirmVisible = false },
            icon = { Icon(imageVector = UniRutaIcons.Play, contentDescription = null) },
            title = { Text(text = "¿Iniciar recorrido?") },
            text = {
                Text(
                    text = "Se solicitará el permiso de ubicación para mostrar tu posición en el mapa y se " +
                        "activará el monitoreo con el acelerómetro y el giroscopio mientras la aplicación esté abierta."
                )
            },
            confirmButton = {
                LinkButton(
                    text = "Iniciar",
                    onClick = {
                        confirmVisible = false
                        onStartTrip()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { confirmVisible = false }) }
        )
    }
}
