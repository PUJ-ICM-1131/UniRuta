package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Vehicle
import com.uniruta.app.data.model.VehicleStatus
import com.uniruta.app.ui.components.FormField
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.SelectableChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.theme.UniRutaIcons

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminVehicleFormScreen(
    vehicle: Vehicle?,
    onSave: (String, String, VehicleStatus) -> String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var plate by rememberSaveable(vehicle?.id) { mutableStateOf(vehicle?.plate.orEmpty()) }
    var capacity by rememberSaveable(vehicle?.id) { mutableStateOf(vehicle?.capacity?.toString().orEmpty()) }
    var status by rememberSaveable(vehicle?.id) { mutableStateOf(vehicle?.status ?: VehicleStatus.ACTIVE) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    UniRutaScreen(
        title = if (vehicle == null) "Nuevo vehículo" else "Editar vehículo",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(
                text = "Guardar vehículo",
                onClick = {
                    val result = onSave(plate, capacity, status)
                    errorMessage = result
                    if (result == null) onBack()
                }
            )
        }
    ) {
        FormField(
            label = "Placa",
            value = plate,
            onValueChange = { plate = it },
            placeholder = "ABC-123",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next)
        )
        FormField(
            label = "Capacidad",
            value = capacity,
            onValueChange = { capacity = it },
            suffix = "puestos",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        SectionTitle(text = "Estado", modifier = Modifier.padding(top = 8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehicleStatus.entries.forEach { option ->
                SelectableChip(label = option.label, selected = status == option, onClick = { status = option })
            }
        }
        NoticeBanner(
            text = "Los vehículos inactivos o en mantenimiento no estarán disponibles para nuevos recorridos.",
            icon = UniRutaIcons.Info,
            tone = StatusTone.INFO
        )

        errorMessage?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }
    }
}
