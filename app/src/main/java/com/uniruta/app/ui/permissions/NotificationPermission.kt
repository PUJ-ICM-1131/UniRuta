package com.uniruta.app.ui.permissions

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.uniruta.app.notifications.canPostNotifications
import com.uniruta.app.ui.components.LinkButton
import com.uniruta.app.ui.components.NoticeBanner
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun NotificationPermissionPrompt(
    purpose: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.canPostNotifications()) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requested = true
        granted = context.canPostNotifications()
    }

    LifecycleResumeEffect(Unit) {
        granted = context.canPostNotifications()
        onPauseOrDispose { }
    }

    if (granted || dismissed) return

    val canAskDirectly = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !requested
    NoticeBanner(
        title = if (requested) "Notificaciones desactivadas" else "Activa las notificaciones",
        text = if (requested) {
            "UniRuta sigue funcionando sin notificaciones. Si cambias de opinión, puedes habilitarlas desde los ajustes del sistema."
        } else {
            "$purpose Solo se muestran avisos de acciones realizadas en este dispositivo."
        },
        icon = UniRutaIcons.Bell,
        tone = StatusTone.INFO,
        modifier = modifier,
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LinkButton(
                    text = if (canAskDirectly) "Activar" else "Abrir ajustes",
                    onClick = {
                        if (canAskDirectly) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            context.openNotificationSettings()
                        }
                    }
                )
                LinkButton(text = "Ahora no", onClick = { dismissed = true })
            }
        }
    )
}

fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    try {
        startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        openAppSettings()
    }
}
