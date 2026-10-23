package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.DangerButton
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.InfoRow
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun ReservationDetailScreen(
    row: ReservationRow,
    passAvailable: Boolean,
    onOpenPass: () -> Unit,
    onCompletePayment: () -> Unit,
    onCancelReservation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reservation = row.reservation
    val trip = row.trip
    val payment = row.payment
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    val paymentIncomplete = reservation.status == ReservationStatus.PENDING && (
        payment == null || (
            payment.method == PaymentMethod.TRANSFER &&
                (payment.status == PaymentStatus.PENDING || payment.status == PaymentStatus.REJECTED)
            )
        )
    val hasActions = passAvailable || paymentIncomplete || reservation.isActive

    UniRutaScreen(
        title = "Detalle de la reserva",
        onBack = onBack,
        modifier = modifier,
        bottomBar = if (hasActions) {
            {
                if (passAvailable) {
                    PrimaryButton(text = "Ver pase digital", onClick = onOpenPass, icon = UniRutaIcons.QrCode)
                }
                if (paymentIncomplete) {
                    SecondaryButton(text = "Completar pago", onClick = onCompletePayment, icon = UniRutaIcons.Cash)
                }
                if (reservation.isActive) {
                    DangerButton(text = "Cancelar reserva", onClick = { confirmCancel = true }, icon = UniRutaIcons.Close)
                }
            }
        } else {
            null
        }
    ) {
        Text(
            text = trip?.route?.name ?: "Reserva ${reservation.id}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ChipRow(modifier = Modifier.padding(horizontal = 4.dp)) {
            StatusChip(label = "Reserva: ${reservation.status.label}", tone = reservation.status.tone())
            payment?.let { StatusChip(label = "Pago: ${it.status.label}", tone = it.status.tone()) }
        }

        SectionTitle(text = "Recorrido", modifier = Modifier.padding(top = 4.dp))
        if (trip == null) {
            EmptyState(icon = UniRutaIcons.Route, message = "El recorrido asociado ya no está disponible.")
        } else {
            UniRutaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                InfoRow(label = "Ruta", value = trip.route.name)
                InfoRow(label = "Fecha", value = trip.date.asShortText())
                InfoRow(label = "Hora de salida", value = trip.departureTime.asText())
                InfoRow(label = "Vehículo", value = trip.vehiclePlate)
                InfoRow(label = "Conductor", value = trip.driverName, showDivider = false)
            }
        }

        SectionTitle(text = "Reserva y pago", modifier = Modifier.padding(top = 4.dp))
        UniRutaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            InfoRow(label = "Identificador", value = reservation.id)
            InfoRow(label = "Fecha de reserva", value = reservation.reservationDate.asShortText())
            InfoRow(label = "Método de pago", value = payment?.method?.label ?: "Sin seleccionar")
            payment?.rejectionReason?.let { InfoRow(label = "Motivo de rechazo", value = it.label) }
            InfoRow(
                label = "Valor",
                value = (payment?.amount ?: trip?.price ?: 0).asPriceText(),
                valueStyle = PriceStyle,
                valueColor = MaterialTheme.colorScheme.primary,
                showDivider = payment?.receiptName != null
            )
            payment?.receiptName?.let { name ->
                InfoRow(label = "Comprobante", value = name, icon = UniRutaIcons.Image, showDivider = false)
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            icon = { Icon(imageVector = UniRutaIcons.Warning, contentDescription = null) },
            title = { Text(text = "¿Cancelar la reserva?") },
            text = { Text(text = "Liberarás tu cupo en ${trip?.route?.name ?: "este recorrido"}. Esta acción no se puede deshacer.") },
            confirmButton = {
                LinkButton(
                    text = "Cancelar reserva",
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        confirmCancel = false
                        onCancelReservation()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Volver", onClick = { confirmCancel = false }) }
        )
    }
}
