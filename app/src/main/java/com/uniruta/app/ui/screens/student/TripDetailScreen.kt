package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.VehiclePosition
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.ui.components.InfoGrid
import com.uniruta.app.ui.components.InfoItem
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.RouteMapCard
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.StopTimeline
import com.uniruta.app.ui.components.TripHeading
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asDistanceText
import com.uniruta.app.ui.format.asDurationText
import com.uniruta.app.ui.format.asMetersDistanceText
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asSecondsDurationText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun TripDetailScreen(
    trip: Trip,
    alreadyReserved: Boolean,
    pathState: RoutePathState?,
    demoVehicle: VehiclePosition?,
    onRetryRoute: () -> Unit,
    onReserve: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapInteracting by remember { mutableStateOf(false) }
    val calculatedPath = (pathState as? RoutePathState.Ready)?.path?.takeIf { it.isCalculated }

    UniRutaScreen(
        title = "Detalle del recorrido",
        onBack = onBack,
        modifier = modifier,
        scrollEnabled = !mapInteracting,
        bottomBar = {
            if (alreadyReserved) {
                Text(
                    text = "Ya tienes una reserva activa para este recorrido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (!trip.isBookable) {
                Text(
                    text = "Este recorrido no tiene cupos disponibles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PrimaryButton(
                text = "Reservar cupo",
                onClick = onReserve,
                enabled = trip.isBookable && !alreadyReserved
            )
        }
    ) {
        TripHeading(
            title = trip.route.name,
            subtitle = "Hacia ${trip.route.destination}",
            chipLabel = if (trip.isBookable) "Disponible" else "Sin cupos",
            chipTone = if (trip.isBookable) StatusTone.POSITIVE else StatusTone.NEGATIVE
        )

        SectionTitle(text = "Vista del recorrido")
        RouteMapCard(
            route = trip.route,
            pathState = pathState,
            vehicle = demoVehicle,
            vehicleTitle = "Vehículo " + trip.vehiclePlate,
            onRetry = onRetryRoute,
            onMapInteraction = { mapInteracting = it }
        )

        SectionTitle(text = "Información del recorrido", modifier = Modifier.padding(top = 8.dp))
        InfoGrid(
            items = listOf(
                InfoItem(UniRutaIcons.Calendar, "Fecha", trip.date.asShortText()),
                InfoItem(UniRutaIcons.Clock, "Hora de salida", trip.departureTime.asText()),
                InfoItem(UniRutaIcons.Truck, "Vehículo", trip.vehiclePlate),
                InfoItem(UniRutaIcons.User, "Conductor", trip.driverName),
                InfoItem(UniRutaIcons.Users, "Cupos", "${trip.availableSeats} / ${trip.totalSeats} disponibles"),
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
                ),
                InfoItem(UniRutaIcons.Cash, "Precio por cupo", trip.price.asPriceText(), emphasized = true)
            )
        )

        SectionTitle(text = "Paradas", modifier = Modifier.padding(top = 8.dp))
        StopTimeline(stops = trip.route.orderedStops.map { it.name })
    }
}
