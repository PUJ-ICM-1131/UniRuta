package com.uniruta.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun UniRutaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, borderColor),
            content = body
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, borderColor),
            content = body
        )
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 14.dp
) {
    val colors = tone.colors()
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.content,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun NeutralIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(UniRutaTheme.colors.infoTile),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun NoticeBanner(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.INFO,
    title: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    val colors = tone.colors()
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (title != null) {
                        Text(text = title, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(text = text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (action != null) {
                Box(modifier = Modifier.padding(start = 32.dp, top = 4.dp)) { action() }
            }
        }
    }
}

@Composable
fun HeroStatus(
    icon: ImageVector,
    tone: StatusTone,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    chips: @Composable () -> Unit = {}
) {
    val colors = tone.colors()
    val halo = when (tone) {
        StatusTone.POSITIVE -> UniRutaTheme.colors.successHalo
        else -> colors.container.copy(alpha = 0.45f)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(halo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(colors.container, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        chips()
    }
}

@Composable
fun ProgressTrack(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = UniRutaTheme.colors.onSuccessContainer,
    trackColor: Color = UniRutaTheme.colors.progressTrack
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val colors = tone.colors()
    val initials = name.split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(44.dp)
            .background(colors.container, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            color = colors.content
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    UniRutaCard(modifier = modifier, contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(icon = icon, tone = StatusTone.INFO)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            action?.invoke()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = if (centered) {
            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.spacedBy(8.dp)
        },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
fun StatusBarScrim(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(UniRutaTheme.colors.headerContainer)
    )
}
