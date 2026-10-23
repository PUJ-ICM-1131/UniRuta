package com.uniruta.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    suffix: String? = null,
    prefix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            trailingIcon = trailingContent,
            suffix = suffix?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge) } },
            prefix = prefix?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge) } },
            supportingText = supportingText?.let { { Text(text = it) } },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedPlaceholderColor = UniRutaTheme.colors.mutedIcon,
                focusedPlaceholderColor = UniRutaTheme.colors.mutedIcon
            )
        )
    }
}

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        },
        leadingIcon = if (selected) {
            { Icon(imageVector = UniRutaIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, UniRutaTheme.colors.outlineStrong)
        },
        modifier = modifier.heightIn(min = 44.dp)
    )
}
