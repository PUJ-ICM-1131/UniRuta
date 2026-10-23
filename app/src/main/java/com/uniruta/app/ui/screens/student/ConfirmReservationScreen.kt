package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.components.InfoRow
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.StopTimeline
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.TotalStyle
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun ConfirmReservationScreen(
    trip: Trip,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stops = trip.route.orderedStops
    val mainStops = when {
        stops.size <= 3 -> stops
        else -> listOf(stops.first(), stops[stops.size / 2], stops.last())
    }

    UniRutaScreen(
        title = "Confirmar reserva",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(text = "Confirmar reserva", onClick = onConfirm, enabled = trip.isBookable)
            SecondaryButton(text = "Cancelar", onClick = onCancel)
        }
    ) {
        SectionTitle(text = "Resumen")
        UniRutaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            InfoRow(label = "Recorrido", value = trip.route.name)
            InfoRow(label = "Fecha", value = trip.date.asShortText())
            InfoRow(label = "Hora de salida", value = trip.departureTime.asText())
            InfoRow(label = "Cupo", value = "1 cupo")
            InfoRow(
                label = "Total a pagar",
                value = trip.price.asPriceText(),
                valueStyle = TotalStyle,
                valueColor = MaterialTheme.colorScheme.primary,
                showDivider = false
            )
        }

        SectionTitle(text = "Paradas principales", modifier = Modifier.padding(top = 4.dp))
        StopTimeline(stops = mainStops.map { it.name }, numbered = false)
        if (stops.size > mainStops.size) {
            Text(
                text = "El recorrido tiene ${stops.size} paradas en total.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        NoticeBanner(
            text = "Al reservar, tu cupo quedará apartado mientras completas el proceso de pago.",
            icon = UniRutaIcons.Info,
            tone = StatusTone.WARNING
        )
    }
}
