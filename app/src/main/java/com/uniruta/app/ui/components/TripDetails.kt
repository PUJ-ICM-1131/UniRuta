package com.uniruta.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.ui.theme.ValueStyle

@Immutable
data class InfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val emphasized: Boolean = false
)

@Composable
fun InfoGrid(
    items: List<InfoItem>,
    modifier: Modifier = Modifier
) {
    UniRutaCard(modifier = modifier, verticalSpacing = 18.dp) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { item ->
                    InfoGridCell(item = item, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoGridCell(item: InfoItem, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NeutralIconTile(icon = item.icon)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.value,
                style = if (item.emphasized) {
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                } else {
                    ValueStyle
                },
                color = if (item.emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StopTimeline(
    stops: List<String>,
    modifier: Modifier = Modifier,
    numbered: Boolean = true
) {
    UniRutaCard(modifier = modifier) {
        stops.forEachIndexed { index, name ->
            val endpoint = index == 0 || index == stops.lastIndex
            val last = index == stops.lastIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .heightIn(min = if (last) 40.dp else 52.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StopMarker(number = if (numbered) index + 1 else null, filled = endpoint)
                    if (!last) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .width(2.dp)
                                .weight(1f)
                                .background(UniRutaTheme.colors.connector)
                        )
                    }
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (endpoint) FontWeight.Medium else FontWeight.Normal
                    ),
                    modifier = Modifier.padding(top = 3.dp, bottom = if (last) 0.dp else 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StopMarker(number: Int?, filled: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(if (filled) primary else MaterialTheme.colorScheme.surface, CircleShape)
            .border(BorderStroke(2.dp, primary), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (number != null) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (filled) MaterialTheme.colorScheme.onPrimary else primary
            )
        }
    }
}

@Composable
fun TripHeading(
    title: String,
    subtitle: String,
    chipLabel: String,
    chipTone: StatusTone,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StatusChip(label = chipLabel, tone = chipTone, modifier = Modifier.padding(top = 2.dp))
    }
}
