package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.ui.components.ChipRow
import com.uniruta.app.ui.components.HeroStatus
import com.uniruta.app.ui.components.InfoList
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.viewmodel.ValidationOutcome

@Composable
fun BoardingResultScreen(
    outcome: ValidationOutcome,
    passenger: TripPassenger?,
    routeName: String,
    onRegisterBoarding: () -> Unit,
    onScanAgain: () -> Unit,
    onBackToTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boarded = passenger?.reservation?.status == ReservationStatus.BOARDED
    val valid = outcome == ValidationOutcome.VALID
    val canRegister = valid && passenger?.canRegisterBoarding == true
    val title = when {
        valid && boarded -> "Abordaje registrado"
        valid -> "Pase válido"
        outcome == ValidationOutcome.ALREADY_USED -> "Pase ya utilizado"
        outcome == ValidationOutcome.WRONG_TRIP -> "Pase de otro recorrido"
        outcome == ValidationOutcome.NOT_CONFIRMED -> "Reserva sin confirmar"
        else -> "Pase no válido"
    }
    val message = when {
        valid && boarded -> "El estudiante quedó registrado como abordado y su pase ya no puede volver a usarse."
        valid -> "Verifica los datos y registra el abordaje del estudiante."
        outcome == ValidationOutcome.ALREADY_USED -> "Este pase ya fue utilizado y no permite un nuevo abordaje."
        outcome == ValidationOutcome.WRONG_TRIP -> "Este pase no corresponde al recorrido actual."
        outcome == ValidationOutcome.NOT_CONFIRMED -> "La reserva asociada a este pase todavía no está confirmada."
        else -> "El código leído no corresponde a ningún pase emitido."
    }
    val tone = when {
        valid -> StatusTone.POSITIVE
        outcome == ValidationOutcome.NOT_FOUND -> StatusTone.NEGATIVE
        else -> StatusTone.WARNING
    }

    UniRutaScreen(
        title = "Resultado de validación",
        onBack = onBackToTrip,
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        bottomBar = {
            if (valid && !boarded) {
                PrimaryButton(
                    text = "Registrar abordaje",
                    onClick = onRegisterBoarding,
                    enabled = canRegister,
                    icon = UniRutaIcons.UserCheck
                )
            }
            SecondaryButton(text = "Validar otro pase", onClick = onScanAgain, icon = UniRutaIcons.QrCode)
            LinkButton(
                text = "Volver al recorrido",
                onClick = onBackToTrip,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    ) {
        HeroStatus(
            icon = when (tone) {
                StatusTone.POSITIVE -> UniRutaIcons.Check
                StatusTone.NEGATIVE -> UniRutaIcons.Close
                else -> UniRutaIcons.Warning
            },
            tone = tone,
            title = title,
            subtitle = message
        ) {
            if (passenger != null) {
                ChipRow(modifier = Modifier.padding(top = 4.dp), centered = true) {
                    StatusChip(label = "Reserva: ${passenger.reservation.status.label}", tone = passenger.reservation.status.tone())
                    StatusChip(label = "Pago: ${passenger.payment.status.label}", tone = passenger.payment.status.tone())
                    passenger.pass?.let { StatusChip(label = "Pase: ${it.status.label}", tone = it.status.tone()) }
                }
            }
        }

        if (passenger != null) {
            SectionTitle(text = "Datos del pasajero")
            InfoList(
                rows = listOf(
                    "Estudiante" to passenger.studentName,
                    "Ruta" to routeName.ifBlank { "—" },
                    "Reserva" to passenger.reservation.id
                )
            )
            if (valid && !boarded && !canRegister) {
                NoticeBanner(
                    text = "El pago todavía no está confirmado. Confírmalo en la lista de pasajeros antes de registrar el abordaje.",
                    icon = UniRutaIcons.Info,
                    tone = StatusTone.WARNING
                )
            }
        }
    }
}
