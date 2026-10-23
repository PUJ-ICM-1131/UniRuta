package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.DigitalPass
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.PassQrCode
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asLongText
import com.uniruta.app.ui.format.asText
import com.uniruta.app.ui.theme.Navy
import com.uniruta.app.ui.theme.TokenStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.ValueStyle

@Composable
fun StudentPassScreen(
    pass: DigitalPass?,
    trip: Trip?,
    studentName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Mi pase",
        onBack = onBack,
        modifier = modifier,
        verticalSpacing = 16.dp
    ) {
        if (pass == null) {
            EmptyState(
                icon = UniRutaIcons.QrCode,
                message = "Todavía no tienes un pase disponible. Se genera cuando tu reserva y tu pago quedan confirmados."
            )
            return@UniRutaScreen
        }

        PassCard(pass = pass, trip = trip, studentName = studentName)

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = UniRutaIcons.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Presenta este pase al conductor al abordar.", style = ValueStyle)
                Text(
                    text = "El código representa únicamente el identificador del pase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PassCard(
    pass: DigitalPass,
    trip: Trip?,
    studentName: String
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy)
                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UniRutaIcons.Bus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "UniRuta",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f)
                )
                StatusChip(label = pass.status.label, tone = pass.status.tone())
            }

            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = trip?.route?.name ?: "Recorrido", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PassField(label = "Fecha", value = trip?.date?.asLongText() ?: "—", modifier = Modifier.weight(1f))
                    PassField(label = "Hora de salida", value = trip?.departureTime?.asText() ?: "—", modifier = Modifier.weight(1f))
                }
                PassField(label = "Estudiante", value = studentName.ifBlank { "—" })
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(2.dp)
            ) {
                drawLine(
                    color = outline,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, outline),
                    modifier = Modifier.fillMaxWidth(0.74f)
                ) {
                    PassQrCode(token = pass.token, modifier = Modifier.padding(6.dp))
                }
                Text(
                    text = pass.token,
                    style = TokenStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PassField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = ValueStyle)
    }
}
