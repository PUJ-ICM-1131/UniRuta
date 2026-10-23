package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.IncidentType
import com.uniruta.app.data.storage.StoredImage
import com.uniruta.app.ui.components.FormField
import com.uniruta.app.ui.components.HeroStatus
import com.uniruta.app.ui.components.IconTile
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.SelectableChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.StoredImagePreview
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asFileSizeText
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.ui.theme.ValueStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportIncidentScreen(
    evidence: StoredImage?,
    evidenceBusy: Boolean,
    evidenceError: String?,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSubmit: (IncidentType, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by rememberSaveable { mutableStateOf(IncidentType.TRAFFIC) }
    var description by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    if (submitted) {
        UniRutaScreen(
            title = "Reportar novedad",
            onBack = onBack,
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
            bottomBar = { PrimaryButton(text = "Volver al recorrido", onClick = onBack) }
        ) {
            HeroStatus(
                icon = UniRutaIcons.Check,
                tone = StatusTone.POSITIVE,
                title = "Novedad registrada",
                subtitle = "Quedó asociada al recorrido en curso."
            )
        }
        return
    }

    UniRutaScreen(
        title = "Reportar novedad",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(
                text = "Registrar novedad",
                onClick = {
                    onSubmit(selectedType, description)
                    submitted = true
                },
                enabled = description.isNotBlank() && !evidenceBusy,
                icon = UniRutaIcons.Send
            )
        }
    ) {
        SectionTitle(text = "Tipo de novedad")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IncidentType.entries.forEach { type ->
                SelectableChip(label = type.label, selected = selectedType == type, onClick = { selectedType = type })
            }
        }

        FormField(
            label = "Descripción",
            value = description,
            onValueChange = { description = it },
            placeholder = "Describe lo ocurrido durante el recorrido",
            singleLine = false,
            minLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.padding(top = 8.dp)
        )

        SectionTitle(text = "Evidencia fotográfica", modifier = Modifier.padding(top = 8.dp))
        Text(
            text = "Toma la fotografía con la cámara del dispositivo. Se guarda en el almacenamiento privado de la aplicación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (evidence == null) {
            EmptyPhotoSlot(busy = evidenceBusy)
        } else {
            UniRutaCard(contentPadding = PaddingValues(14.dp), verticalSpacing = 12.dp) {
                StoredImagePreview(path = evidence.path, contentDescription = "Fotografía de evidencia")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(icon = UniRutaIcons.Image, tone = StatusTone.POSITIVE, size = 40.dp, iconSize = 22.dp, cornerRadius = 12.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = evidence.displayName,
                            style = ValueStyle,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis
                        )
                        Text(
                            text = "Fotografía capturada · ${evidence.sizeBytes.asFileSizeText()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinkButton(text = "Quitar", onClick = onRemovePhoto, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        evidenceError?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }

        SecondaryButton(
            text = if (evidence == null) "Agregar fotografía" else "Tomar otra fotografía",
            onClick = onTakePhoto,
            enabled = !evidenceBusy,
            icon = UniRutaIcons.Camera
        )
    }
}

@Composable
private fun EmptyPhotoSlot(busy: Boolean) {
    val dashColor = UniRutaTheme.colors.outlineStrong
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        if (busy) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            Text(text = "Guardando la fotografía…", style = MaterialTheme.typography.bodyMedium)
        } else {
            Icon(
                imageVector = UniRutaIcons.Image,
                contentDescription = null,
                tint = UniRutaTheme.colors.mutedIcon,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Sin fotografías adjuntas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
