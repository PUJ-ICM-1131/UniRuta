package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asDistanceText
import com.uniruta.app.ui.format.asDurationText
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun StudentTripsScreen(
    trips: List<Trip>,
    onTripSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Recorridos disponibles",
        onBack = onBack,
        modifier = modifier
    ) {
        if (trips.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Route, message = "No hay recorridos programados por ahora.")
            return@UniRutaScreen
        }
        val destinations = trips.map { it.route.destination }.distinct()
        SectionLabel(
            text = if (destinations.size == 1) "Hacia ${destinations.single()}" else "Recorridos programados"
        )
        trips.forEach { trip ->
            TripCard(trip = trip, onClick = { onTripSelected(trip.id) })
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    val available = trip.isBookable
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    UniRutaCard(
        onClick = if (available) onClick else null,
        containerColor = if (available) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = trip.route.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (available) MaterialTheme.colorScheme.onSurface else muted,
                modifier = Modifier.weight(1f)
            )
            StatusChip(
                label = if (available) "Disponible" else "Sin cupos",
                tone = if (available) StatusTone.POSITIVE else StatusTone.NEGATIVE
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("${trip.date.asShortText()} · ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            color = if (available) MaterialTheme.colorScheme.onSurface else muted
                        )
                    ) { append(trip.departureTime.asText()) }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = trip.price.asPriceText(),
                style = PriceStyle,
                color = if (available) MaterialTheme.colorScheme.primary else muted
            )
        }
        CardDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetaItem(
                icon = UniRutaIcons.Users,
                text = "${trip.availableSeats} / ${trip.totalSeats} cupos",
                color = if (available) muted else MaterialTheme.colorScheme.error
            )
            MetaItem(icon = UniRutaIcons.Timer, text = trip.estimatedDurationMinutes.asDurationText())
            MetaItem(icon = UniRutaIcons.ArrowsHorizontal, text = trip.estimatedDistanceKm.asDistanceText())
        }
    }
}
