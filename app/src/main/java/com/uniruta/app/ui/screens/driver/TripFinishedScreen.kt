package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.components.HeroStatus
import com.uniruta.app.ui.components.InfoList
import com.uniruta.app.ui.components.Metric
import com.uniruta.app.ui.components.MetricGrid
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asLongText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun TripFinishedScreen(
    trip: Trip,
    boardedCount: Int,
    passengerCount: Int,
    sensorAnomalyCount: Int,
    demoAnomalyCount: Int,
    incidentCount: Int,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Recorrido finalizado",
        onBack = null,
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        bottomBar = { PrimaryButton(text = "Volver al inicio", onClick = onBackHome) }
    ) {
        HeroStatus(
            icon = UniRutaIcons.Check,
            tone = StatusTone.POSITIVE,
            title = "Recorrido finalizado",
            subtitle = "El seguimiento de ubicación y sensores se ha detenido."
        )

        SectionTitle(text = "Resumen")
        InfoList(
            rows = listOf(
                "Ruta" to trip.route.name,
                "Fecha" to trip.date.asLongText(),
                "Hora de salida" to trip.departureTime.asText(),
                "Vehículo" to trip.vehiclePlate,
                "Pasajeros con reserva" to passengerCount.toString(),
                "Estado" to trip.status.label
            )
        )

        MetricGrid(
            columns = 3,
            metrics = listOf(
                Metric(
                    label = "Abordajes registrados",
                    value = boardedCount,
                    icon = UniRutaIcons.UserCheck,
                    tone = StatusTone.POSITIVE
                ),
                Metric(
                    label = "Movimientos anómalos",
                    value = sensorAnomalyCount,
                    icon = UniRutaIcons.Warning,
                    tone = StatusTone.WARNING
                ),
                Metric(label = "Novedades", value = incidentCount, icon = UniRutaIcons.Note, tone = StatusTone.INFO)
            )
        )

        if (demoAnomalyCount > 0) {
            NoticeBanner(
                text = if (demoAnomalyCount == 1) {
                    "Además se registró 1 evento de demostración, que no cuenta como lectura real de los sensores."
                } else {
                    "Además se registraron $demoAnomalyCount eventos de demostración, que no cuentan como lecturas reales de los sensores."
                },
                icon = UniRutaIcons.Info,
                tone = StatusTone.NEUTRAL
            )
        }
    }
}
