package com.uniruta.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.MetricStyle
import com.uniruta.app.ui.theme.UniRutaTheme

@Immutable
data class Metric(
    val label: String,
    val value: Int,
    val icon: ImageVector? = null,
    val highlighted: Boolean = false,
    val tone: StatusTone = StatusTone.INFO
)

@Composable
fun MetricGrid(
    metrics: List<Metric>,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { metric ->
                    MetricCard(
                        metric = metric,
                        stacked = columns > 2,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun MetricCard(
    metric: Metric,
    modifier: Modifier = Modifier,
    stacked: Boolean = false
) {
    val extended = UniRutaTheme.colors
    UniRutaCard(
        modifier = modifier,
        containerColor = if (metric.highlighted) extended.warningContainer else MaterialTheme.colorScheme.surface,
        borderColor = if (metric.highlighted) extended.warningBorder else MaterialTheme.colorScheme.outlineVariant,
        contentPadding = PaddingValues(14.dp),
        verticalSpacing = 6.dp
    ) {
        val valueColor = if (metric.highlighted) extended.onWarningContainer else MaterialTheme.colorScheme.primary
        val iconTone = if (metric.highlighted) StatusTone.WARNING else metric.tone
        if (stacked) {
            metric.icon?.let { IconTile(icon = it, tone = iconTone, size = 36.dp, iconSize = 20.dp, cornerRadius = 10.dp) }
            Text(text = metric.value.toString(), style = MetricStyle, color = valueColor)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = metric.value.toString(),
                    style = MetricStyle,
                    color = valueColor,
                    modifier = Modifier.weight(1f)
                )
                metric.icon?.let { IconTile(icon = it, tone = iconTone, size = 40.dp, iconSize = 22.dp, cornerRadius = 12.dp) }
            }
        }
        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (metric.highlighted) extended.onWarningContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
