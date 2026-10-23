package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Payment
import com.uniruta.app.data.model.Reservation
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

data class ReservationRow(
    val reservation: Reservation,
    val trip: Trip?,
    val payment: Payment?
)

@Composable
fun StudentReservationsScreen(
    rows: List<ReservationRow>,
    onReservationSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Mis reservas",
        onBack = onBack,
        modifier = modifier
    ) {
        if (rows.isEmpty()) {
            EmptyState(
                icon = UniRutaIcons.CalendarCheck,
                message = "Todavía no tienes reservas. Empieza por consultar los recorridos disponibles."
            )
            return@UniRutaScreen
        }
        rows.forEach { row ->
            ReservationCard(row = row, onClick = { onReservationSelected(row.reservation.id) })
        }
    }
}

@Composable
private fun ReservationCard(
    row: ReservationRow,
    onClick: () -> Unit
) {
    val active = row.reservation.isActive
    UniRutaCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = row.trip?.route?.name ?: "Recorrido no disponible",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                row.trip?.let { trip ->
                    Text(
                        text = "${trip.date.asShortText()} · ${trip.departureTime.asText()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = UniRutaIcons.ChevronRight,
                contentDescription = null,
                tint = UniRutaTheme.colors.mutedIcon,
                modifier = Modifier.size(24.dp)
            )
        }
        ChipRow {
            StatusChip(label = "Reserva: ${row.reservation.status.label}", tone = row.reservation.status.tone())
            row.payment?.let { StatusChip(label = "Pago: ${it.status.label}", tone = it.status.tone()) }
        }
        CardDivider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MetaItem(
                icon = row.payment?.method?.icon() ?: UniRutaIcons.Cash,
                text = row.payment?.method?.label ?: "Sin método de pago",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = (row.payment?.amount ?: row.trip?.price ?: 0).asPriceText(),
                style = PriceStyle,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
