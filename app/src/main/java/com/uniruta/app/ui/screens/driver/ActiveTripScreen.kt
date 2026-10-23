package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.AnomalyEvent
import com.uniruta.app.data.model.EventOrigin
import com.uniruta.app.data.model.SensorSource
import com.uniruta.app.data.model.TrackingStatus
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.VehiclePosition
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.DangerButton
import com.uniruta.app.ui.components.IconTile
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.ProgressTrack
import com.uniruta.app.ui.components.RouteMapCard
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTile
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asCoordinateText
import com.uniruta.app.ui.format.asMagnitudeText
import com.uniruta.app.ui.format.asReadingText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.ValueStyle
import com.uniruta.app.viewmodel.MotionSensorState
import kotlin.math.roundToInt

@Composable
fun ActiveTripScreen(
    trip: Trip,
    pathState: RoutePathState?,
    gpsStatus: TrackingStatus,
    deviceLocation: VehiclePosition?,
    sensorStatus: TrackingStatus,
    accelerometer: MotionSensorState,
    gyroscope: MotionSensorState,
    boardedCount: Int,
    passengerCount: Int,
    anomalies: List<AnomalyEvent>,
    incidentCount: Int,
    onRequestLocation: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRetryRoute: () -> Unit,
    onAddDemoAnomaly: () -> Unit,
    onValidateBoarding: () -> Unit,
    onOpenPassengers: () -> Unit,
    onReportIncident: () -> Unit,
    onFinishTrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var mapInteracting by remember { mutableStateOf(false) }
    val sensorEvents = anomalies.count { it.origin == EventOrigin.SENSOR }
    val demoEvents = anomalies.size - sensorEvents
    val boardingComplete = passengerCount > 0 && boardedCount == passengerCount

    UniRutaScreen(
        title = "Recorrido en curso",
        onBack = onBack,
        modifier = modifier,
        scrollEnabled = !mapInteracting,
        bottomBar = {
            PrimaryButton(text = "Validar abordaje", onClick = onValidateBoarding, icon = UniRutaIcons.QrCode)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Ver pasajeros",
                    onClick = onOpenPassengers,
                    icon = UniRutaIcons.Users,
                    compact = true,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = if (incidentCount == 0) "Reportar novedad" else "Novedades ($incidentCount)",
                    onClick = onReportIncident,
                    icon = UniRutaIcons.Note,
                    compact = true,
                    modifier = Modifier.weight(1f)
                )
            }
            DangerButton(text = "Finalizar recorrido", onClick = { confirmVisible = true }, icon = UniRutaIcons.Stop)
        }
    ) {
        UniRutaCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp), verticalSpacing = 10.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = trip.route.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                StatusChip(label = trip.status.label, tone = trip.status.tone())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetaItem(icon = UniRutaIcons.Clock, text = "Salida ${trip.departureTime.asText()}")
                MetaItem(icon = UniRutaIcons.Truck, text = trip.vehiclePlate)
            }
        }

        RouteMapCard(
            route = trip.route,
            pathState = pathState,
            vehicle = deviceLocation,
            vehicleTitle = "Tu ubicación",
            onRetry = onRetryRoute,
            onMapInteraction = { mapInteracting = it }
        )

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusTile(
                icon = UniRutaIcons.Crosshair,
                title = "Ubicación",
                detail = locationSummary(gpsStatus, deviceLocation),
                chipLabel = gpsStatus.label,
                chipTone = gpsStatus.tone(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatusTile(
                icon = UniRutaIcons.Activity,
                title = "Sensores",
                detail = "Acelerómetro ${accelerometer.status.label.lowercase()} · " +
                    "Giroscopio ${gyroscope.status.label.lowercase()}",
                chipLabel = sensorStatus.label,
                chipTone = sensorStatus.tone(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusTile(
                icon = UniRutaIcons.UserCheck,
                title = "Abordaje",
                detail = "$boardedCount de $passengerCount pasajeros abordados",
                chipLabel = if (boardingComplete) "Completo" else "En curso",
                chipTone = if (boardingComplete) StatusTone.POSITIVE else StatusTone.INFO,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ProgressTrack(fraction = if (passengerCount == 0) 0f else boardedCount.toFloat() / passengerCount)
            }
            StatusTile(
                icon = UniRutaIcons.Warning,
                iconTone = StatusTone.WARNING,
                title = "Movimientos anómalos",
                detail = when {
                    anomalies.isEmpty() -> "Sin eventos registrados"
                    demoEvents == 0 -> "$sensorEvents detectados por sensores"
                    else -> "$sensorEvents por sensores · $demoEvents de demostración"
                },
                chipLabel = if (anomalies.isEmpty()) "Sin eventos" else "Revisar",
                chipTone = if (anomalies.isEmpty()) StatusTone.NEUTRAL else StatusTone.WARNING,
                showChipDot = anomalies.isEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        LocationProblem(
            status = gpsStatus,
            onRequestLocation = onRequestLocation,
            onOpenLocationSettings = onOpenLocationSettings,
            onOpenAppSettings = onOpenAppSettings
        )

        SectionTitle(text = "Lecturas en tiempo real", modifier = Modifier.padding(top = 8.dp))
        LiveReadingsCard(
            gpsStatus = gpsStatus,
            deviceLocation = deviceLocation,
            accelerometer = accelerometer,
            gyroscope = gyroscope
        )

        SectionTitle(text = "Registro de movimiento", modifier = Modifier.padding(top = 8.dp))
        AnomalyLogCard(anomalies = anomalies, onAddDemoAnomaly = onAddDemoAnomaly)
    }

    if (confirmVisible) {
        AlertDialog(
            onDismissRequest = { confirmVisible = false },
            icon = { Icon(imageVector = UniRutaIcons.Stop, contentDescription = null) },
            title = { Text(text = "¿Finalizar recorrido?") },
            text = {
                Text(
                    text = "Se detendrán el seguimiento GPS y el monitoreo de movimiento. " +
                        "No podrás seguir validando abordajes de este recorrido."
                )
            },
            confirmButton = {
                LinkButton(
                    text = "Finalizar",
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        confirmVisible = false
                        onFinishTrip()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { confirmVisible = false }) }
        )
    }
}

@Composable
private fun LocationProblem(
    status: TrackingStatus,
    onRequestLocation: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    when (status) {
        TrackingStatus.PERMISSION_DENIED -> NoticeBanner(
            title = "Ubicación sin permiso",
            text = "Tu posición no se está registrando. Concede el permiso para mostrarla en el mapa.",
            icon = UniRutaIcons.Crosshair,
            tone = StatusTone.WARNING,
            action = {
                Row {
                    LinkButton(text = "Permitir ubicación", onClick = onRequestLocation)
                    LinkButton(text = "Abrir ajustes", onClick = onOpenAppSettings)
                }
            }
        )
        TrackingStatus.DISABLED -> NoticeBanner(
            title = "Ubicación desactivada",
            text = "Activa la ubicación del dispositivo para recibir lecturas del GPS.",
            icon = UniRutaIcons.Crosshair,
            tone = StatusTone.WARNING,
            action = { LinkButton(text = "Activar ubicación", onClick = onOpenLocationSettings) }
        )
        TrackingStatus.ERROR -> NoticeBanner(
            title = "Error de ubicación",
            text = "No fue posible obtener la ubicación.",
            icon = UniRutaIcons.AlertCircle,
            tone = StatusTone.NEGATIVE,
            action = { LinkButton(text = "Reintentar", onClick = onRequestLocation, icon = UniRutaIcons.Refresh) }
        )
        TrackingStatus.UNAVAILABLE -> NoticeBanner(
            text = "Google Play services no está disponible en este dispositivo; no es posible usar el GPS.",
            icon = UniRutaIcons.AlertCircle,
            tone = StatusTone.NEGATIVE
        )
        else -> Unit
    }
}

@Composable
private fun LiveReadingsCard(
    gpsStatus: TrackingStatus,
    deviceLocation: VehiclePosition?,
    accelerometer: MotionSensorState,
    gyroscope: MotionSensorState
) {
    UniRutaCard(verticalSpacing = 12.dp) {
        ReadingRow(
            title = "GPS del dispositivo",
            detail = locationDetail(gpsStatus, deviceLocation),
            status = gpsStatus
        )
        CardDivider()
        ReadingRow(
            title = SensorSource.ACCELEROMETER.label,
            detail = sensorReading(SensorSource.ACCELEROMETER, accelerometer),
            status = accelerometer.status
        )
        CardDivider()
        ReadingRow(
            title = SensorSource.GYROSCOPE.label,
            detail = sensorReading(SensorSource.GYROSCOPE, gyroscope),
            status = gyroscope.status
        )
    }
}

@Composable
private fun ReadingRow(title: String, detail: String, status: TrackingStatus) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = ValueStyle)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StatusChip(label = status.label, tone = status.tone())
    }
}

@Composable
private fun AnomalyLogCard(
    anomalies: List<AnomalyEvent>,
    onAddDemoAnomaly: () -> Unit
) {
    UniRutaCard(verticalSpacing = 12.dp) {
        if (anomalies.isEmpty()) {
            Text(
                text = "Aún no se han registrado movimientos anómalos en este recorrido.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        anomalies.asReversed().forEachIndexed { index, anomaly ->
            if (index > 0) CardDivider()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconTile(
                    icon = anomaly.type.icon(),
                    tone = if (anomaly.origin == EventOrigin.SENSOR) StatusTone.WARNING else StatusTone.NEUTRAL,
                    size = 40.dp,
                    iconSize = 22.dp,
                    cornerRadius = 12.dp
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = anomaly.type.label, style = ValueStyle)
                    Text(
                        text = anomaly.magnitude?.let {
                            "${anomaly.type.source.label} · ${it.asMagnitudeText()} ${anomaly.type.source.unit}"
                        } ?: "${anomaly.type.source.label} · sin medición",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusChip(
                        label = anomaly.origin.label,
                        tone = if (anomaly.origin == EventOrigin.SENSOR) StatusTone.WARNING else StatusTone.NEUTRAL,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(text = anomaly.time.asText(), style = ValueStyle, textAlign = TextAlign.End)
            }
        }
        CardDivider()
        Text(
            text = "Modo demostración: agrega un evento simulado, marcado como tal y contado aparte de las lecturas reales.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinkButton(text = "Agregar evento de demostración", onClick = onAddDemoAnomaly, icon = UniRutaIcons.Plus)
    }
}

private fun locationSummary(status: TrackingStatus, location: VehiclePosition?): String = when (status) {
    TrackingStatus.ACTIVE -> location?.accuracyMeters?.let { "Lectura real · ±${it.roundToInt()} m" }
        ?: "Lectura real del GPS"
    TrackingStatus.SEARCHING -> "Esperando la primera lectura"
    TrackingStatus.PAUSED -> "En pausa con la app en segundo plano"
    TrackingStatus.PERMISSION_DENIED -> "Sin permiso: no se registra tu posición"
    TrackingStatus.DISABLED -> "Ubicación del dispositivo desactivada"
    TrackingStatus.UNAVAILABLE -> "GPS no disponible"
    TrackingStatus.ERROR -> "No fue posible obtener la ubicación"
    TrackingStatus.INACTIVE -> "Seguimiento detenido"
}

private fun locationDetail(status: TrackingStatus, location: VehiclePosition?): String = when (status) {
    TrackingStatus.ACTIVE -> location?.let { position ->
        val accuracy = position.accuracyMeters?.let { " · ±${it.roundToInt()} m" }.orEmpty()
        "${position.point.asCoordinateText()}$accuracy · ${position.updatedAt.asText()}"
    } ?: "Recibiendo lecturas del GPS."
    TrackingStatus.SEARCHING -> "Esperando la primera lectura del GPS."
    TrackingStatus.PAUSED -> location?.let { "Última lectura: ${it.point.asCoordinateText()} · ${it.updatedAt.asText()}" }
        ?: "En pausa mientras la aplicación no está visible."
    TrackingStatus.PERMISSION_DENIED -> "Sin permiso de ubicación: tu posición no se está registrando."
    TrackingStatus.DISABLED -> "La ubicación del dispositivo está desactivada."
    TrackingStatus.UNAVAILABLE -> "Google Play services no está disponible en este dispositivo."
    TrackingStatus.ERROR -> "No fue posible obtener la ubicación."
    TrackingStatus.INACTIVE -> "Seguimiento detenido."
}

private fun sensorReading(source: SensorSource, state: MotionSensorState): String =
    state.reading?.let { reading ->
        "x ${reading.x.asReadingText()} · y ${reading.y.asReadingText()} · z ${reading.z.asReadingText()} ${source.unit}"
    } ?: when (state.status) {
        TrackingStatus.UNAVAILABLE -> "Este dispositivo no tiene el sensor."
        TrackingStatus.SEARCHING -> "Esperando lecturas."
        TrackingStatus.PAUSED -> "En pausa mientras la aplicación no está visible."
        TrackingStatus.ERROR -> "No fue posible leer el sensor."
        else -> "Sin lecturas."
    }
