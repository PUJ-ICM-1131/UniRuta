package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.InitialsAvatar
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.ProgressTrack
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.TotalStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun DriverPassengersScreen(
    passengers: List<TripPassenger>,
    onConfirmCashPayment: (String) -> Unit,
    onRegisterBoarding: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cashDialogFor by rememberSaveable { mutableStateOf<String?>(null) }
    val boarded = passengers.count { it.hasBoarded }

    UniRutaScreen(
        title = "Pasajeros",
        onBack = onBack,
        modifier = modifier
    ) {
        if (passengers.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Users, message = "Este recorrido todavía no tiene pasajeros registrados.")
            return@UniRutaScreen
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Abordados", style = MaterialTheme.typography.bodySmall)
                    Text(text = "$boarded de ${passengers.size}", style = TotalStyle)
                }
                ProgressTrack(
                    fraction = boarded.toFloat() / passengers.size,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(140.dp)
                )
            }
        }

        passengers.forEach { passenger ->
            PassengerCard(
                passenger = passenger,
                onConfirmCash = { cashDialogFor = passenger.reservation.id },
                onRegisterBoarding = { onRegisterBoarding(passenger.reservation.id) }
            )
        }
    }

    val pendingReservationId = cashDialogFor
    if (pendingReservationId != null) {
        AlertDialog(
            onDismissRequest = { cashDialogFor = null },
            icon = { Icon(imageVector = UniRutaIcons.Cash, contentDescription = null) },
            title = { Text(text = "Confirmar pago en efectivo") },
            text = { Text(text = "Confirma que recibiste el pago en efectivo del estudiante antes de registrar su abordaje.") },
            confirmButton = {
                LinkButton(
                    text = "Confirmar",
                    onClick = {
                        onConfirmCashPayment(pendingReservationId)
                        cashDialogFor = null
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { cashDialogFor = null }) }
        )
    }
}

@Composable
private fun PassengerCard(
    passenger: TripPassenger,
    onConfirmCash: () -> Unit,
    onRegisterBoarding: () -> Unit
) {
    UniRutaCard(
        borderColor = if (passenger.awaitsCashOnBoarding) UniRutaTheme.colors.warningBorder else MaterialTheme.colorScheme.outlineVariant,
        contentPadding = PaddingValues(16.dp),
        verticalSpacing = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InitialsAvatar(
                name = passenger.studentName,
                tone = if (passenger.hasBoarded) StatusTone.POSITIVE else StatusTone.WARNING
            )
            Text(text = passenger.studentName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(
                label = if (passenger.hasBoarded) "Abordó" else "Sin abordar",
                tone = if (passenger.hasBoarded) StatusTone.POSITIVE else StatusTone.WARNING
            )
        }
        ChipRow {
            StatusChip(label = "Reserva: ${passenger.reservation.status.label}", tone = passenger.reservation.status.tone())
            StatusChip(label = "Pago: ${passenger.payment.status.label}", tone = passenger.payment.status.tone())
        }
        CardDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            MetaItem(
                icon = passenger.payment.method.icon(),
                text = passenger.payment.method.label,
                modifier = Modifier.weight(1f)
            )
            Text(text = passenger.payment.amount.asPriceText(), style = PriceStyle, color = MaterialTheme.colorScheme.primary)
        }
        when {
            passenger.awaitsCashOnBoarding -> PrimaryButton(
                text = "Confirmar pago al abordar",
                onClick = onConfirmCash,
                icon = UniRutaIcons.Cash
            )
            passenger.canRegisterBoarding -> SecondaryButton(
                text = "Registrar abordaje",
                onClick = onRegisterBoarding,
                icon = UniRutaIcons.UserCheck
            )
        }
    }
}
