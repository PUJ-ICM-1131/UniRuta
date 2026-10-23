package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.SelectableChip
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaFab
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons

private val filters = listOf(
    null to "Todos",
    TripStatus.SCHEDULED to "Programados",
    TripStatus.IN_PROGRESS to "En curso",
    TripStatus.COMPLETED to "Finalizados"
)

@Composable
fun AdminTripsScreen(
    trips: List<Trip>,
    onScheduleTrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable { mutableStateOf<TripStatus?>(null) }
    val visibleTrips = trips.filter { selectedFilter == null || it.status == selectedFilter }

    UniRutaScreen(
        title = "Recorridos",
        onBack = onBack,
        modifier = modifier,
        floatingAction = { UniRutaFab(text = "Programar", icon = UniRutaIcons.Plus, onClick = onScheduleTrip) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (status, label) ->
                SelectableChip(label = label, selected = selectedFilter == status, onClick = { selectedFilter = status })
            }
        }
        if (visibleTrips.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Calendar, message = "No hay recorridos con este estado.")
        }
        visibleTrips.forEach { trip -> AdminTripCard(trip = trip) }
    }
}

@Composable
private fun AdminTripCard(trip: Trip) {
    UniRutaCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = trip.route.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(label = trip.status.label, tone = trip.status.tone())
        }
        Text(
            text = buildAnnotatedString {
                append("${trip.date.asShortText()} · ")
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)) {
                    append("Salida ${trip.departureTime.asText()}")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CardDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetaItem(icon = UniRutaIcons.Truck, text = trip.vehiclePlate)
            MetaItem(icon = UniRutaIcons.Users, text = "${trip.availableSeats} de ${trip.totalSeats} cupos libres")
        }
        MetaItem(icon = UniRutaIcons.User, text = trip.driverName)
    }
}
