package com.uniruta.app.ui.screens.driver

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.ui.components.FeatureItem
import com.uniruta.app.ui.components.RoleHomeScaffold
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.permissions.NotificationPermissionPrompt
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun DriverHomeScreen(
    user: SessionUser,
    hasActiveTrip: Boolean,
    onOpenTrips: () -> Unit,
    onStartTrip: () -> Unit,
    onValidateBoarding: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoleHomeScaffold(
        user = user,
        headline = "Gestiona los recorridos que tienes a cargo.",
        features = listOf(
            FeatureItem(
                title = "Recorridos asignados",
                description = "Revisa las rutas que tienes programadas.",
                icon = UniRutaIcons.Calendar,
                tone = StatusTone.INFO,
                onClick = onOpenTrips
            ),
            FeatureItem(
                title = if (hasActiveTrip) "Continuar recorrido" else "Iniciar recorrido",
                description = if (hasActiveTrip) {
                    "Vuelve al recorrido en curso y a su seguimiento."
                } else {
                    "Marca la salida y activa el seguimiento de ubicación y sensores."
                },
                icon = if (hasActiveTrip) UniRutaIcons.Activity else UniRutaIcons.Play,
                tone = if (hasActiveTrip) StatusTone.POSITIVE else StatusTone.WARNING,
                onClick = onStartTrip
            ),
            FeatureItem(
                title = "Validar abordaje",
                description = "Verifica el pase de cada estudiante que sube al vehículo.",
                icon = UniRutaIcons.QrCode,
                tone = StatusTone.POSITIVE,
                onClick = onValidateBoarding
            )
        ),
        onLogout = onLogout,
        modifier = modifier,
        footer = {
            NotificationPermissionPrompt(
                purpose = "UniRuta te avisará cuando inicies o finalices un recorrido y cuando registres una novedad.",
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    )
}
