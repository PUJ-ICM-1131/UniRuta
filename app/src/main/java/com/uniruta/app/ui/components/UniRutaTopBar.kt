package com.uniruta.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.SectionLabelStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.ui.theme.ValueStyle

@Composable
fun UniRutaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = UniRutaTheme.colors.headerContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = if (onBack != null) 8.dp else 20.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = UniRutaIcons.ArrowBack, contentDescription = "Volver")
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                modifier = Modifier.semantics { heading() }
            )
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = SectionLabelStyle,
        color = UniRutaTheme.colors.sectionLabel,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .padding(bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    valueStyle: TextStyle = ValueStyle,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UniRutaTheme.colors.mutedIcon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                style = valueStyle,
                color = valueColor,
                textAlign = TextAlign.End
            )
        }
        if (showDivider) {
            HorizontalDivider(color = UniRutaTheme.colors.divider)
        }
    }
}

@Composable
fun InfoList(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    UniRutaCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        rows.forEachIndexed { index, (label, value) ->
            InfoRow(label = label, value = value, showDivider = index < rows.lastIndex)
        }
    }
}

@Composable
fun MetaItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UniRutaTheme.colors.mutedIcon,
            modifier = Modifier.size(18.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = color, maxLines = 1)
    }
}

@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = UniRutaTheme.colors.divider)
    }
}
