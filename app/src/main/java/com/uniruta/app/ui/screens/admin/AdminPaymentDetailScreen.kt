package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.RejectionReason
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.DangerButton
import com.uniruta.app.ui.components.IconTile
import com.uniruta.app.ui.components.InfoRow
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.SelectableChip
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asLongText
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.PriceStyle
import com.uniruta.app.ui.theme.TokenStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.ValueStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPaymentDetailScreen(
    entry: TripPassenger,
    trip: Trip?,
    onApprove: () -> Unit,
    onReject: (RejectionReason) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var approveVisible by rememberSaveable { mutableStateOf(false) }
    var rejectVisible by rememberSaveable { mutableStateOf(false) }
    var selectedReason by rememberSaveable { mutableStateOf(RejectionReason.UNREADABLE_RECEIPT) }
    val payment = entry.payment
    val cashOnBoarding = payment.method == PaymentMethod.CASH_BOARDING
    val confirmed = payment.status == PaymentStatus.CONFIRMED
    val rejected = payment.status == PaymentStatus.REJECTED
    val actionable = entry.reviewableByAdmin && !confirmed
    val canReject = actionable && payment.method == PaymentMethod.TRANSFER && !rejected

    UniRutaScreen(
        title = "Detalle del pago",
        onBack = onBack,
        modifier = modifier,
        bottomBar = if (actionable) {
            {
                PrimaryButton(
                    text = if (payment.method == PaymentMethod.CASH_POINT) "Confirmar pago recibido" else "Aprobar pago",
                    onClick = { approveVisible = true },
                    icon = UniRutaIcons.Check
                )
                if (canReject) {
                    DangerButton(text = "Rechazar pago", onClick = { rejectVisible = true }, icon = UniRutaIcons.Close)
                }
            }
        } else {
            null
        }
    ) {
        Text(
            text = payment.method.label,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ChipRow(modifier = Modifier.padding(horizontal = 4.dp)) {
            StatusChip(label = "Pago: ${payment.status.label}", tone = payment.status.tone())
            StatusChip(label = "Reserva: ${entry.reservation.status.label}", tone = entry.reservation.status.tone())
        }

        SectionTitle(text = "Información", modifier = Modifier.padding(top = 4.dp))
        UniRutaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            InfoRow(label = "Estudiante", value = entry.studentName)
            InfoRow(label = "Reserva", value = entry.reservation.id)
            InfoRow(label = "Ruta", value = trip?.route?.name ?: "No disponible")
            if (trip != null) {
                InfoRow(label = "Fecha", value = trip.date.asLongText())
                InfoRow(label = "Hora", value = trip.departureTime.asText())
            }
            InfoRow(
                label = "Monto",
                value = payment.amount.asPriceText(),
                valueStyle = PriceStyle,
                valueColor = MaterialTheme.colorScheme.primary
            )
            InfoRow(label = "Método", value = payment.method.label, showDivider = payment.rejectionReason != null)
            payment.rejectionReason?.let { InfoRow(label = "Motivo del rechazo", value = it.label, showDivider = false) }
        }

        payment.receiptName?.let { name ->
            SectionTitle(text = "Comprobante", modifier = Modifier.padding(top = 4.dp))
            UniRutaCard(contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconTile(icon = UniRutaIcons.FileCheck, tone = StatusTone.INFO)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, style = ValueStyle, maxLines = 1, overflow = TextOverflow.MiddleEllipsis)
                        Text(
                            text = "Archivo registrado en la reserva",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        entry.pass?.let { pass ->
            NoticeBanner(
                title = "Pase emitido · ${pass.status.label}",
                text = "Identificador del pase:",
                icon = UniRutaIcons.QrCode,
                tone = StatusTone.POSITIVE,
                action = { Text(text = pass.token, style = TokenStyle) }
            )
        }

        when {
            cashOnBoarding -> NoticeBanner(
                text = "Este pago se confirma durante el abordaje y no requiere revisión administrativa.",
                icon = UniRutaIcons.Info,
                tone = StatusTone.NEUTRAL
            )
            confirmed -> NoticeBanner(
                title = "Pago confirmado",
                text = "La reserva fue confirmada y el pase ya está disponible.",
                icon = UniRutaIcons.CheckCircle,
                tone = StatusTone.POSITIVE
            )
            !entry.reviewableByAdmin -> NoticeBanner(
                text = "La reserva fue cancelada; este pago ya no requiere revisión.",
                icon = UniRutaIcons.Info,
                tone = StatusTone.NEUTRAL
            )
            rejected -> NoticeBanner(
                title = "Pago rechazado",
                text = "Puedes aprobarlo si el estudiante presenta un comprobante válido.",
                icon = UniRutaIcons.AlertCircle,
                tone = StatusTone.NEGATIVE
            )
        }
    }

    if (approveVisible) {
        AlertDialog(
            onDismissRequest = { approveVisible = false },
            icon = { Icon(imageVector = UniRutaIcons.CheckCircle, contentDescription = null) },
            title = { Text(text = "¿Aprobar el pago?") },
            text = { Text(text = "La reserva quedará confirmada y se emitirá el pase digital del estudiante.") },
            confirmButton = {
                LinkButton(
                    text = "Aprobar",
                    onClick = {
                        approveVisible = false
                        onApprove()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { approveVisible = false }) }
        )
    }

    if (rejectVisible) {
        AlertDialog(
            onDismissRequest = { rejectVisible = false },
            icon = { Icon(imageVector = UniRutaIcons.Close, contentDescription = null) },
            title = { Text(text = "¿Rechazar el pago?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "La reserva permanecerá pendiente y no se emitirá pase. Selecciona el motivo:")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RejectionReason.entries.forEach { reason ->
                            SelectableChip(
                                label = reason.label,
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                LinkButton(
                    text = "Rechazar",
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        rejectVisible = false
                        onReject(selectedReason)
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { rejectVisible = false }) }
        )
    }
}
