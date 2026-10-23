package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.components.IconTile
import com.uniruta.app.ui.components.NeutralIconTile
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.SectionTitle
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.StoredImagePreview
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.theme.TotalStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.ui.theme.ValueStyle

@Composable
fun TransferPaymentScreen(
    amount: Int,
    reservationId: String,
    receiptName: String?,
    receiptPath: String?,
    importInProgress: Boolean,
    receiptError: String?,
    onPickReceipt: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Transferencia",
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            PrimaryButton(
                text = "Enviar comprobante",
                onClick = onSubmit,
                enabled = receiptName != null && !importInProgress,
                icon = UniRutaIcons.Send
            )
        }
    ) {
        SectionTitle(text = "Datos de la transferencia")
        UniRutaCard(verticalSpacing = 14.dp) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Monto a transferir", style = MaterialTheme.typography.bodySmall)
                    Text(text = amount.asPriceText(), style = TotalStyle)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeutralIconTile(icon = UniRutaIcons.Hash, size = 40.dp)
                Column {
                    Text(
                        text = "Referencia de la reserva",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = reservationId, style = ValueStyle)
                }
            }
        }

        SectionTitle(text = "Comprobante", modifier = Modifier.padding(top = 8.dp))
        Text(
            text = "Adjunta una imagen del comprobante desde tu galería para que sea revisado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (receiptName != null) {
            UniRutaCard(
                borderColor = UniRutaTheme.colors.onSuccessContainer.copy(alpha = 0.35f),
                contentPadding = PaddingValues(14.dp),
                verticalSpacing = 12.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    IconTile(icon = UniRutaIcons.Image, tone = StatusTone.POSITIVE)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = receiptName,
                            style = ValueStyle,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = UniRutaIcons.CheckCircle,
                                contentDescription = null,
                                tint = UniRutaTheme.colors.onSuccessContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Comprobante seleccionado",
                                style = MaterialTheme.typography.labelSmall,
                                color = UniRutaTheme.colors.onSuccessContainer
                            )
                        }
                    }
                }
                if (receiptPath != null) {
                    StoredImagePreview(path = receiptPath, contentDescription = "Vista previa del comprobante")
                }
            }
        }

        if (importInProgress) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Text(
                    text = "Guardando una copia privada de la imagen…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        receiptError?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }

        SecondaryButton(
            text = if (receiptName == null) "Seleccionar comprobante" else "Reemplazar comprobante",
            onClick = onPickReceipt,
            enabled = !importInProgress,
            icon = UniRutaIcons.Upload
        )
    }
}
