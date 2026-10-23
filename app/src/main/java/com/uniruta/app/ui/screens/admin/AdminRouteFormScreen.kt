package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Route
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.FormField
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.theme.UniRutaIcons

private val stopListSaver = listSaver<SnapshotStateList<String>, String>(
    save = { it.toList() },
    restore = { mutableStateListOf<String>().apply { addAll(it) } }
)

private val textCapitalization = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)

@Composable
fun AdminRouteFormScreen(
    route: Route?,
    onSave: (String, String, String, List<String>) -> String?,
    onDeactivate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(route?.id) { mutableStateOf(route?.name.orEmpty()) }
    var origin by rememberSaveable(route?.id) { mutableStateOf(route?.origin.orEmpty()) }
    var destination by rememberSaveable(route?.id) { mutableStateOf(route?.destination.orEmpty()) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var deactivateVisible by rememberSaveable { mutableStateOf(false) }
    val stops = rememberSaveable(route?.id, saver = stopListSaver) {
        mutableStateListOf<String>().apply {
            addAll(route?.orderedStops?.map { it.name } ?: listOf("", ""))
        }
    }

    UniRutaScreen(
        title = if (route == null) "Nueva ruta" else "Editar ruta",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(
                text = "Guardar ruta",
                onClick = {
                    val result = onSave(name, origin, destination, stops.toList())
                    errorMessage = result
                    if (result == null) onBack()
                }
            )
            if (route != null && route.active) {
                LinkButton(
                    text = "Desactivar ruta",
                    onClick = { deactivateVisible = true },
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    ) {
        if (route != null && !route.active) {
            NoticeBanner(
                text = "Esta ruta está inactiva y no aparece al programar nuevos recorridos.",
                icon = UniRutaIcons.Info,
                tone = StatusTone.NEUTRAL
            )
        }
        FormField(
            label = "Nombre de la ruta",
            value = name,
            onValueChange = { name = it },
            placeholder = "Ej. Chía → Javeriana",
            keyboardOptions = textCapitalization
        )
        FormField(
            label = "Origen",
            value = origin,
            onValueChange = { origin = it },
            keyboardOptions = textCapitalization
        )
        FormField(
            label = "Destino",
            value = destination,
            onValueChange = { destination = it },
            keyboardOptions = textCapitalization
        )

        SectionTitle(
            text = "Paradas",
            trailing = if (stops.size == 1) "1 parada" else "${stops.size} paradas",
            modifier = Modifier.padding(top = 8.dp)
        )
        UniRutaCard(verticalSpacing = 12.dp) {
            stops.forEachIndexed { index, stopName ->
                if (index > 0) CardDivider()
                StopEditor(
                    index = index,
                    value = stopName,
                    isLast = index == stops.lastIndex,
                    canRemove = stops.size > 2,
                    onValueChange = { stops[index] = it },
                    onMoveUp = {
                        val previous = stops[index - 1]
                        stops[index - 1] = stops[index]
                        stops[index] = previous
                    },
                    onMoveDown = {
                        val next = stops[index + 1]
                        stops[index + 1] = stops[index]
                        stops[index] = next
                    },
                    onRemove = { stops.removeAt(index) }
                )
            }
        }
        SecondaryButton(text = "Agregar parada", onClick = { stops.add("") }, icon = UniRutaIcons.Plus)

        Text(
            text = "Las paradas nuevas no tienen ubicación geográfica y no aparecen en el mapa hasta que se les asigne una.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        errorMessage?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }
    }

    if (deactivateVisible) {
        AlertDialog(
            onDismissRequest = { deactivateVisible = false },
            icon = { Icon(imageVector = UniRutaIcons.Warning, contentDescription = null) },
            title = { Text(text = "¿Desactivar ruta?") },
            text = {
                Text(
                    text = "La ruta dejará de estar disponible para programar nuevos recorridos. " +
                        "Los recorridos ya programados no se modifican."
                )
            },
            confirmButton = {
                LinkButton(
                    text = "Desactivar",
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        deactivateVisible = false
                        onDeactivate()
                        onBack()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { deactivateVisible = false }) }
        )
    }
}

@Composable
private fun StopEditor(
    index: Int,
    value: String,
    isLast: Boolean,
    canRemove: Boolean,
    onValueChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val endpoint = index == 0 || isLast
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (endpoint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (endpoint) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "Parada ${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )
            IconButton(onClick = onMoveUp, enabled = index > 0) {
                Icon(imageVector = UniRutaIcons.ChevronUp, contentDescription = "Subir parada ${index + 1}")
            }
            IconButton(onClick = onMoveDown, enabled = !isLast) {
                Icon(imageVector = UniRutaIcons.ChevronDown, contentDescription = "Bajar parada ${index + 1}")
            }
            IconButton(onClick = onRemove, enabled = canRemove) {
                Icon(
                    imageVector = UniRutaIcons.Trash,
                    contentDescription = "Eliminar parada ${index + 1}",
                    tint = if (canRemove) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(text = "Nombre de la parada") },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = textCapitalization,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        )
    }
}
