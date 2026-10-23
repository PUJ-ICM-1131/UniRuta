package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asLongText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

private val statusSections = listOf(
    TripStatus.IN_PROGRESS to "En curso",
    TripStatus.SCHEDULED to "Programados",
    TripStatus.COMPLETED to "Finalizados",
    TripStatus.CANCELLED to "Cancelados"
)

@Composable
fun DriverTripsScreen(
    trips: List<Trip>,
    driverName: String,
    onTripSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Recorridos asignados",
        onBack = onBack,
        modifier = modifier
    ) {
        if (trips.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Calendar, message = "No tienes recorridos asignados por ahora.")
            return@UniRutaScreen
        }
        statusSections.forEach { (status, label) ->
            val group = trips.filter { it.status == status }.sortedWith(compareBy({ it.date }, { it.departureTime }))
            if (group.isNotEmpty()) {
                SectionLabel(text = label, modifier = Modifier.padding(top = 4.dp))
                group.forEach { trip ->
                    DriverTripCard(trip = trip, driverName = driverName, onClick = { onTripSelected(trip.id) })
                }
            }
        }
    }
}

@Composable
private fun DriverTripCard(
    trip: Trip,
    driverName: String,
    onClick: () -> Unit
) {
    UniRutaCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = trip.route.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(label = trip.status.label, tone = trip.status.tone())
        }
        Text(
            text = buildAnnotatedString {
                append("${trip.date.asLongText()} · ")
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)) {
                    append("Salida ${trip.departureTime.asText()}")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CardDivider()
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetaItem(icon = UniRutaIcons.Truck, text = trip.vehiclePlate)
            MetaItem(
                icon = UniRutaIcons.Users,
                text = "${trip.totalSeats - trip.availableSeats} / ${trip.totalSeats} ocupados",
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = UniRutaIcons.ChevronRight,
                contentDescription = null,
                tint = UniRutaTheme.colors.mutedIcon,
                modifier = Modifier.size(24.dp)
            )
        }
        MetaItem(icon = UniRutaIcons.User, text = "Asignado a $driverName")
    }
}
