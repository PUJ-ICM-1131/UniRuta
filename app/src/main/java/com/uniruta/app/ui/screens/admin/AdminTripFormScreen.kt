package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.Vehicle
import com.uniruta.app.ui.components.FormField
import com.uniruta.app.ui.components.HeroStatus
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.SelectableChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.theme.UniRutaIcons
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminTripFormScreen(
    routes: List<Route>,
    vehicles: List<Vehicle>,
    drivers: List<String>,
    onSchedule: (String?, LocalDate?, LocalTime?, String?, String?, String, String) -> String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var routeId by rememberSaveable { mutableStateOf(routes.firstOrNull()?.id) }
    var vehicleId by rememberSaveable { mutableStateOf(vehicles.firstOrNull()?.id) }
    var driverName by rememberSaveable { mutableStateOf(drivers.firstOrNull()) }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).format(dateFormat)) }
    var timeText by rememberSaveable { mutableStateOf("06:30") }
    var seats by rememberSaveable { mutableStateOf("20") }
    var price by rememberSaveable { mutableStateOf("6000") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var scheduled by rememberSaveable { mutableStateOf(false) }
    var datePickerVisible by rememberSaveable { mutableStateOf(false) }
    var timePickerVisible by rememberSaveable { mutableStateOf(false) }

    if (scheduled) {
        UniRutaScreen(
            title = "Programar recorrido",
            onBack = onBack,
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
            bottomBar = { PrimaryButton(text = "Volver a recorridos", onClick = onBack) }
        ) {
            HeroStatus(
                icon = UniRutaIcons.Check,
                tone = StatusTone.POSITIVE,
                title = "Recorrido programado",
                subtitle = "Ya aparece en la lista de recorridos."
            )
        }
        return
    }

    UniRutaScreen(
        title = "Programar recorrido",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(
                text = "Programar recorrido",
                onClick = {
                    val result = onSchedule(
                        routeId,
                        parseDate(dateText),
                        parseTime(timeText),
                        vehicleId,
                        driverName,
                        seats,
                        price
                    )
                    errorMessage = result
                    if (result == null) scheduled = true
                }
            )
        }
    ) {
        SectionTitle(text = "Ruta activa")
        if (routes.isEmpty()) {
            Text(
                text = "No hay rutas activas disponibles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            routes.forEach { route ->
                SelectableChip(label = route.name, selected = routeId == route.id, onClick = { routeId = route.id })
            }
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(
                label = "Fecha",
                value = dateText,
                onValueChange = { dateText = it },
                placeholder = "dd/MM/aaaa",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                trailingContent = {
                    IconButton(onClick = { datePickerVisible = true }) {
                        Icon(imageVector = UniRutaIcons.Calendar, contentDescription = "Elegir fecha")
                    }
                },
                modifier = Modifier.weight(1f)
            )
            FormField(
                label = "Hora",
                value = timeText,
                onValueChange = { timeText = it },
                placeholder = "HH:mm",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                trailingContent = {
                    IconButton(onClick = { timePickerVisible = true }) {
                        Icon(imageVector = UniRutaIcons.Clock, contentDescription = "Elegir hora")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        SectionTitle(text = "Vehículo activo", modifier = Modifier.padding(top = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vehicles.forEach { vehicle ->
                SelectableChip(
                    label = "${vehicle.plate} · ${vehicle.capacity}",
                    selected = vehicleId == vehicle.id,
                    onClick = { vehicleId = vehicle.id }
                )
            }
        }
        Text(
            text = "Solo se muestran vehículos activos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        SectionTitle(text = "Conductor", modifier = Modifier.padding(top = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            drivers.forEach { driver ->
                SelectableChip(label = driver, selected = driverName == driver, onClick = { driverName = driver })
            }
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(
                label = "Cupos",
                value = seats,
                onValueChange = { seats = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            FormField(
                label = "Precio",
                value = price,
                onValueChange = { price = it },
                prefix = "$ ",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
        }

        errorMessage?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }
    }

    if (datePickerVisible) {
        TripDatePicker(
            initial = parseDate(dateText),
            onDismiss = { datePickerVisible = false },
            onConfirm = { date ->
                dateText = date.format(dateFormat)
                datePickerVisible = false
            }
        )
    }
    if (timePickerVisible) {
        TripTimePicker(
            initial = parseTime(timeText),
            onDismiss = { timePickerVisible = false },
            onConfirm = { time ->
                timeText = time.format(timeFormat)
                timePickerVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePicker(
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: LocalDate.now()).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            LinkButton(
                text = "Aceptar",
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            )
        },
        dismissButton = { LinkButton(text = "Cancelar", onClick = onDismiss) }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTimePicker(
    initial: LocalTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val start = initial ?: LocalTime.of(6, 30)
    val state = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Hora de salida") },
        text = { TimePicker(state = state) },
        confirmButton = {
            LinkButton(text = "Aceptar", onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) })
        },
        dismissButton = { LinkButton(text = "Cancelar", onClick = onDismiss) }
    )
}

private fun parseDate(value: String): LocalDate? = runCatching {
    LocalDate.parse(value.trim(), dateFormat)
}.getOrNull()

private fun parseTime(value: String): LocalTime? = runCatching {
    LocalTime.parse(value.trim(), timeFormat)
}.getOrNull()
