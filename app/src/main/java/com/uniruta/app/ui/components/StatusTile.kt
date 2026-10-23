package com.uniruta.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.ValueStyle

@Composable
fun StatusTile(
    icon: ImageVector,
    title: String,
    detail: String,
    chipLabel: String,
    chipTone: StatusTone,
    modifier: Modifier = Modifier,
    iconTone: StatusTone = StatusTone.INFO,
    showChipDot: Boolean = true,
    onClick: (() -> Unit)? = null,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    UniRutaCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp),
        verticalSpacing = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconTile(icon = icon, tone = iconTone, size = 40.dp, iconSize = 22.dp, cornerRadius = 12.dp)
            StatusChip(label = chipLabel, tone = chipTone, showDot = showChipDot)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = ValueStyle)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        extra()
    }
}
