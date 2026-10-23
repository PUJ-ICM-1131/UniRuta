package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.ui.components.FeatureCard
import com.uniruta.app.ui.components.FeatureItem
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.format.asPriceText
import com.uniruta.app.ui.theme.TotalStyle

@Composable
fun PaymentMethodScreen(
    amount: Int,
    onMethodSelected: (PaymentMethod) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Método de pago",
        onBack = onBack,
        modifier = modifier
    ) {
        Text(
            text = "Selecciona cómo quieres pagar",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total de la reserva:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(text = amount.asPriceText(), style = TotalStyle)
            }
        }
        SectionLabel(text = "Métodos disponibles", modifier = Modifier.padding(top = 8.dp))
        PaymentMethod.entries.forEach { method ->
            FeatureCard(
                item = FeatureItem(
                    title = method.label,
                    description = method.description,
                    icon = method.icon(),
                    tone = method.tone(),
                    onClick = { onMethodSelected(method) }
                )
            )
        }
    }
}
