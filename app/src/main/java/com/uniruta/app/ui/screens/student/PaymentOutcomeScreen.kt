package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Payment
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.Reservation
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.HeroStatus
import com.uniruta.app.ui.components.InfoList
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun PaymentOutcomeScreen(
    reservation: Reservation,
    payment: Payment,
    routeName: String,
    onOpenReservations: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val submitted = payment.method == PaymentMethod.TRANSFER && payment.status == PaymentStatus.UNDER_REVIEW
    val title: String
    val message: String
    when (payment.method) {
        PaymentMethod.TRANSFER -> {
            title = if (submitted) "Comprobante enviado" else "Pago pendiente"
            message = if (submitted) {
                "Tu pago está pendiente de validación."
            } else {
                "Adjunta el comprobante de la transferencia para que sea revisado."
            }
        }
        PaymentMethod.CASH_POINT -> {
            title = "Reserva registrada"
            message = "Realiza el pago en un punto habilitado. El estado cambiará cuando el pago sea confirmado."
        }
        PaymentMethod.CASH_BOARDING -> {
            title = "Reserva registrada"
            message = "El pago será confirmado por el conductor al momento de abordar."
        }
    }

    UniRutaScreen(
        title = "Estado de la reserva",
        onBack = null,
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        bottomBar = {
            PrimaryButton(text = "Ver mis reservas", onClick = onOpenReservations)
            SecondaryButton(text = "Volver al inicio", onClick = onBackHome)
        }
    ) {
        HeroStatus(
            icon = if (submitted || payment.method != PaymentMethod.TRANSFER) UniRutaIcons.Check else UniRutaIcons.Clock,
            tone = if (submitted || payment.method != PaymentMethod.TRANSFER) StatusTone.POSITIVE else StatusTone.WARNING,
            title = title,
            subtitle = message
        ) {
            ChipRow(modifier = Modifier.padding(top = 4.dp), centered = true) {
                StatusChip(label = "Reserva: ${reservation.status.label}", tone = reservation.status.tone())
                StatusChip(label = "Pago: ${payment.status.label}", tone = payment.status.tone())
            }
        }

        SectionTitle(text = "Detalle")
        InfoList(
            rows = listOfNotNull(
                "Recorrido" to routeName,
                "Reserva" to reservation.id,
                "Método de pago" to payment.method.label,
                "Monto" to payment.amount.asPriceText(),
                payment.receiptName?.let { "Comprobante" to it }
            )
        )
    }
}
