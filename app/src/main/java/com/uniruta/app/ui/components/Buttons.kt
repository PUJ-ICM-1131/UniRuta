package com.uniruta.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaTheme

private val ButtonHeight = 52.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = UniRutaTheme.colors.disabledFill,
            disabledContentColor = UniRutaTheme.colors.disabledText
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonHeight)
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = UniRutaTheme.colors.disabledText,
                modifier = Modifier.size(20.dp)
            )
        } else {
            ButtonContent(text = text, icon = icon)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fillWidth: Boolean = true,
    compact: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.dp, UniRutaTheme.colors.outlineStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = if (compact) 10.dp else 16.dp),
        modifier = (if (fillWidth) modifier.fillMaxWidth() else modifier).heightIn(min = ButtonHeight)
    ) {
        ButtonContent(text = text, icon = icon, compact = compact)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.dp, UniRutaTheme.colors.errorBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonHeight)
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun UniRutaFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(imageVector = icon, contentDescription = null) },
        text = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier
    )
}

@Composable
private fun RowScope.ButtonContent(text: String, icon: ImageVector?, compact: Boolean = false) {
    if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(if (compact) 18.dp else 20.dp))
        Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))
    }
    Text(
        text = text,
        style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}
