package com.uniruta.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.device.qr.QrCodeEncoder
import kotlin.math.floor

private const val QUIET_ZONE_MODULES = 4

@Composable
fun PassQrCode(
    token: String,
    modifier: Modifier = Modifier
) {
    val matrix = remember(token) { QrCodeEncoder.encode(token) }

    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        if (matrix == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No fue posible generar el código QR del pase.",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
            return@Surface
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .semantics { contentDescription = "Código QR del pase" }
        ) {
            val modules = matrix.size + QUIET_ZONE_MODULES * 2
            val cell = floor(size.width / modules).coerceAtLeast(1f)
            val origin = floor((size.width - cell * matrix.size) / 2f)
            for (row in 0 until matrix.size) {
                for (column in 0 until matrix.size) {
                    if (!matrix.isDark(row, column)) continue
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(origin + column * cell, origin + row * cell),
                        size = Size(cell, cell)
                    )
                }
            }
        }
    }
}
