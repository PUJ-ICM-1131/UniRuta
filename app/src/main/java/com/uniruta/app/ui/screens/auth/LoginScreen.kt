package com.uniruta.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.mock.MockAccounts
import com.uniruta.app.data.model.UserRole
import com.uniruta.app.ui.components.BrandMark
import com.uniruta.app.ui.components.FormField
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.PrimaryButton
import com.uniruta.app.ui.components.StatusBarScrim
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.icon
import com.uniruta.app.ui.theme.BadgeStyle
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    state: AuthUiState,
    demoHasChanges: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDemoLogin: (UserRole) -> Unit,
    onDismissUnassigned: () -> Unit,
    onResetDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var accountsVisible by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            LoginHeader()

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (!state.firebaseConfigured) {
                    NoticeBanner(
                        title = "Firebase aún no está configurado",
                        text = "El inicio de sesión con correo se habilitará cuando se agregue el archivo de configuración del proyecto. Mientras tanto, usa el acceso de demostración.",
                        icon = UniRutaIcons.Info,
                        tone = StatusTone.INFO
                    )
                }

                state.unassignedEmail?.let { email ->
                    NoticeBanner(
                        title = "Cuenta sin rol asignado",
                        text = "La cuenta $email se autenticó, pero no tiene un rol configurado en UniRuta. No se concedió acceso y la sesión se cerró.",
                        icon = UniRutaIcons.Warning,
                        tone = StatusTone.WARNING,
                        action = { LinkButton(text = "Entendido", onClick = onDismissUnassigned) }
                    )
                }

                FormField(
                    label = "Correo institucional",
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = "nombre@javeriana.edu.co",
                    leadingIcon = UniRutaIcons.Mail,
                    enabled = !state.isLoading,
                    isError = state.errorMessage != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                FormField(
                    label = "Contraseña",
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Ingresa tu contraseña",
                    leadingIcon = UniRutaIcons.Lock,
                    enabled = !state.isLoading,
                    isError = state.errorMessage != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    trailingContent = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) UniRutaIcons.EyeOff else UniRutaIcons.Eye,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                state.errorMessage?.let { message ->
                    NoticeBanner(text = message, icon = UniRutaIcons.AlertCircle, tone = StatusTone.NEGATIVE)
                }

                PrimaryButton(
                    text = "Iniciar sesión",
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    loading = state.isLoading,
                    modifier = Modifier.padding(top = 4.dp)
                )

                DemoAccessCard(
                    enabled = !state.isLoading,
                    demoHasChanges = demoHasChanges,
                    onDemoLogin = onDemoLogin,
                    onResetDemo = { if (demoHasChanges) confirmReset = true }
                )

                LinkButton(
                    text = "Ver cuentas de prueba",
                    onClick = { accountsVisible = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
    }

    if (accountsVisible) {
        DemoAccountsDialog(onDismiss = { accountsVisible = false })
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            icon = { Icon(imageVector = UniRutaIcons.Refresh, contentDescription = null) },
            title = { Text(text = "¿Restablecer la demostración?") },
            text = {
                Text(
                    text = "Se descartarán las reservas, pagos, recorridos, abordajes, novedades y cambios administrativos hechos en los escenarios de demostración, junto con sus fotografías y comprobantes de prueba. No se modifican cuentas de Firebase, contactos de emergencia ni claves de configuración."
                )
            },
            confirmButton = {
                LinkButton(
                    text = "Restablecer",
                    onClick = {
                        confirmReset = false
                        onResetDemo()
                    }
                )
            },
            dismissButton = { LinkButton(text = "Cancelar", onClick = { confirmReset = false }) }
        )
    }
}

@Composable
private fun LoginHeader() {
    Surface(
        color = UniRutaTheme.colors.headerContainer,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BrandMark(size = 64.dp, iconSize = 34.dp, cornerRadius = 18.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "UniRuta",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Tu ruta al campus, en un solo lugar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = UniRutaTheme.colors.headerSubtitle
                )
            }
        }
    }
}

@Composable
private fun DemoAccessCard(
    enabled: Boolean,
    demoHasChanges: Boolean,
    onDemoLogin: (UserRole) -> Unit,
    onResetDemo: () -> Unit
) {
    UniRutaCard(verticalSpacing = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Acceso rápido de demostración",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            Surface(
                color = UniRutaTheme.colors.warningContainer,
                contentColor = UniRutaTheme.colors.onWarningContainer,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "DEMO",
                    style = BadgeStyle,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserRole.entries.forEach { role ->
                Surface(
                    onClick = { onDemoLogin(role) },
                    enabled = enabled,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .heightIn(min = 80.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                    ) {
                        Icon(imageVector = role.icon(), contentDescription = null, modifier = Modifier.size(24.dp))
                        Text(
                            text = role.label,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
        Text(
            text = "Las sesiones de demostración no usan Firebase y abren los escenarios precargados.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusChip(
                label = if (demoHasChanges) "Escenarios con cambios" else "Escenarios sin cambios",
                tone = if (demoHasChanges) StatusTone.WARNING else StatusTone.POSITIVE,
                modifier = Modifier.weight(1f, fill = false)
            )
            LinkButton(
                text = "Restablecer",
                onClick = onResetDemo,
                enabled = enabled && demoHasChanges,
                icon = UniRutaIcons.Refresh
            )
        }
    }
}

@Composable
private fun DemoAccountsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Cuentas de prueba") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MockAccounts.all.forEach { account ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = account.role.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(text = account.name, style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${account.role.label} · ${account.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "Estas personas se usan solo en el modo demostración y entran con los botones de acceso rápido, sin contraseña. El formulario de correo y contraseña valida cuentas reales de Firebase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { LinkButton(text = "Cerrar", onClick = onDismiss) }
    )
}
