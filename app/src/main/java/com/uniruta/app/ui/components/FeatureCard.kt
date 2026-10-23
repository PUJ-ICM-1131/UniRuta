package com.uniruta.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Immutable
data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val tone: StatusTone = StatusTone.INFO,
    val onClick: (() -> Unit)? = null
)

@Composable
fun FeatureCard(
    item: FeatureItem,
    modifier: Modifier = Modifier
) {
    UniRutaCard(
        modifier = modifier,
        onClick = item.onClick,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item.icon?.let { IconTile(icon = it, tone = item.tone) }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.onClick != null) {
                Icon(
                    imageVector = UniRutaIcons.ChevronRight,
                    contentDescription = null,
                    tint = UniRutaTheme.colors.mutedIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
