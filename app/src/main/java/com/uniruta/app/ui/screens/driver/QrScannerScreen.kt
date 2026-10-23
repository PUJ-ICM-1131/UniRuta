package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.mock.DemoScan
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.SecondaryButton
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.theme.ScannerSurface
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun QrScannerScreen(
    routeName: String?,
    scanError: String?,
    demoScans: List<DemoScan>,
    onScanWithCamera: () -> Unit,
    onDemoScan: (DemoScan) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Validar abordaje",
        onBack = onBack,
        modifier = modifier,
        bottomBar = if (routeName != null) {
            { PrimaryButton(text = "Abrir cámara y escanear", onClick = onScanWithCamera, icon = UniRutaIcons.Camera) }
        } else {
            null
        }
    ) {
        if (routeName == null) {
            EmptyState(
                icon = UniRutaIcons.QrCode,
                message = "Para validar abordajes primero debes iniciar un recorrido desde tus recorridos asignados."
            )
            return@UniRutaScreen
        }

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = routeName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(label = "En curso", tone = StatusTone.POSITIVE)
        }

        Surface(
            color = ScannerSurface,
            contentColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = UniRutaIcons.QrCode,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Escanear pase con la cámara",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Se abrirá el escáner de Google Play services en una pantalla propia. Apunta la cámara al código QR del pase; al leerlo volverás aquí con el resultado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center
                )
            }
        }

        scanError?.let { message ->
            NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
        }

        UniRutaCard(
            contentPadding = PaddingValues(16.dp),
            verticalSpacing = 12.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Pases de prueba", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusChip(label = "Modo demostración", tone = StatusTone.WARNING)
            }
            Text(
                text = "Valida tokens precargados sin usar la cámara. Se aplican las mismas reglas que a una lectura real.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            demoScans.forEach { scan ->
                SecondaryButton(text = scan.label, onClick = { onDemoScan(scan) }, icon = UniRutaIcons.QrCode)
            }
        }
    }
}
