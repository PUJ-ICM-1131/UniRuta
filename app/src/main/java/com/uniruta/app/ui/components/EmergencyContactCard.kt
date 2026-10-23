package com.uniruta.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.ValueStyle
import com.uniruta.app.viewmodel.EmergencyContactUiState

@Composable
fun EmergencyContactCard(
    state: EmergencyContactUiState,
    onPickContact: () -> Unit,
    onCall: (String) -> Unit,
    onRemove: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmRemoval by rememberSaveable { mutableStateOf(false) }
    val contact = state.contact

    UniRutaCard(modifier = modifier, verticalSpacing = 14.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconTile(
                icon = UniRutaIcons.Phone,
                tone = if (contact != null) StatusTone.POSITIVE else StatusTone.WARNING
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Contacto de emergencia", style = MaterialTheme.typography.titleMedium)
                if (contact != null) {
                    Text(text = contact.name, style = ValueStyle)
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Elige a una persona de tu agenda para llamarla rápidamente si lo necesitas durante un recorrido.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        StatusChip(
            label = if (contact != null) "Contacto configurado" else "Sin contacto configurado",
            tone = if (contact != null) StatusTone.POSITIVE else StatusTone.WARNING
        )

        state.errorMessage?.let { message ->
            NoticeBanner(
                text = message,
                icon = UniRutaIcons.AlertCircle,
                tone = StatusTone.NEGATIVE,
                action = { LinkButton(text = "Entendido", onClick = onDismissError) }
            )
        }

        when {
            !state.isLoaded || state.isBusy -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
            contact == null -> SecondaryButton(
                text = "Seleccionar contacto",
                onClick = onPickContact,
                icon = UniRutaIcons.UserPlus
            )
            else -> {
                PrimaryButton(
                    text = "Llamar a ${contact.name.substringBefore(' ')}",
                    onClick = { onCall(contact.phoneNumber) },
                    icon = UniRutaIcons.Phone
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "Cambiar",
                        onClick = onPickContact,
                        icon = UniRutaIcons.UserPlus,
                        modifier = Modifier.weight(1f)
                    )
                    DangerButton(
                        text = "Eliminar",
                        onClick = { confirmRemoval = true },
                        icon = UniRutaIcons.Trash,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Text(
            text = "Se guarda solo en este dispositivo y no se envía a ningún servidor. La llamada se confirma en la aplicación de teléfono.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmRemoval && contact != null) {
        AlertDialog(
            onDismissRequest = { confirmRemoval = false },
            icon = { Icon(imageVector = UniRutaIcons.Trash, contentDescription = null) },
            title = { Text(text = "¿Eliminar el contacto de emergencia?") },
            text = {
                Text(text = "${contact.name} dejará de estar configurado en UniRuta. El contacto no se borra de tu agenda.")
            },
            confirmButton = {
                LinkButton(
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        confirmRemoval = false
                        onRemove()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { confirmRemoval = false }) }
        )
    }
}
