package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun AdminPaymentsScreen(
    entries: List<TripPassenger>,
    tripResolver: (String) -> Trip?,
    onPaymentSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attention = entries.filter { it.requiresAdminReview }
    val others = entries.filterNot { it.requiresAdminReview }

    UniRutaScreen(
        title = "Pagos",
        onBack = onBack,
        modifier = modifier
    ) {
        if (entries.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Cash, message = "No hay pagos registrados.")
            return@UniRutaScreen
        }
        if (attention.isNotEmpty()) {
            SectionLabel(text = "Requieren atención · ${attention.size}")
            attention.forEach { entry ->
                PaymentCard(entry = entry, trip = tripResolver(entry.tripId), onClick = { onPaymentSelected(entry.payment.id) })
            }
        }
        if (others.isNotEmpty()) {
            SectionLabel(text = "Otros pagos", modifier = Modifier.padding(top = if (attention.isEmpty()) 0.dp else 8.dp))
            others.forEach { entry ->
                PaymentCard(entry = entry, trip = tripResolver(entry.tripId), onClick = { onPaymentSelected(entry.payment.id) })
            }
        }
    }
}

@Composable
private fun PaymentCard(
    entry: TripPassenger,
    trip: Trip?,
    onClick: () -> Unit
) {
    val attention = entry.requiresAdminReview
    UniRutaCard(
        onClick = onClick,
        borderColor = if (attention) UniRutaTheme.colors.warningBorder else MaterialTheme.colorScheme.outlineVariant,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = entry.studentName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listOfNotNull(trip?.route?.name ?: "Recorrido no disponible", trip?.date?.asShortText())
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (attention) {
                StatusChip(
                    label = if (entry.payment.method == PaymentMethod.CASH_POINT) "Por confirmar" else "Requiere revisión",
                    tone = StatusTone.WARNING
                )
            } else {
                Icon(
                    imageVector = UniRutaIcons.ChevronRight,
                    contentDescription = null,
                    tint = UniRutaTheme.colors.mutedIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        ChipRow {
            StatusChip(label = "Pago: ${entry.payment.status.label}", tone = entry.payment.status.tone())
            StatusChip(label = "Reserva: ${entry.reservation.status.label}", tone = entry.reservation.status.tone())
        }
        CardDivider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MetaItem(icon = entry.payment.method.icon(), text = entry.payment.method.label, modifier = Modifier.weight(1f))
            Text(text = entry.payment.amount.asPriceText(), style = PriceStyle, color = MaterialTheme.colorScheme.primary)
        }
    }
}
