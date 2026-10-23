package com.uniruta.app.ui.screens.student

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.ui.components.EmergencyContactCard
import com.uniruta.app.ui.components.FeatureItem
import com.uniruta.app.ui.components.RoleHomeScaffold
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.permissions.NotificationPermissionPrompt
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.viewmodel.EmergencyContactUiState

@Composable
fun StudentHomeScreen(
    user: SessionUser,
    contactState: EmergencyContactUiState,
    onOpenTrips: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenPass: () -> Unit,
    onPickContact: () -> Unit,
    onCallContact: (String) -> Unit,
    onRemoveContact: () -> Unit,
    onDismissContactError: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoleHomeScaffold(
        user = user,
        headline = "Planea tu recorrido del día en el campus.",
        features = listOf(
            FeatureItem(
                title = "Recorridos disponibles",
                description = "Consulta las rutas y horarios habilitados.",
                icon = UniRutaIcons.Route,
                tone = StatusTone.INFO,
                onClick = onOpenTrips
            ),
            FeatureItem(
                title = "Mis reservas",
                description = "Revisa el estado de tus cupos y pagos.",
                icon = UniRutaIcons.CalendarCheck,
                tone = StatusTone.WARNING,
                onClick = onOpenReservations
            ),
            FeatureItem(
                title = "Mi pase",
                description = "Presenta tu pase al abordar el vehículo.",
                icon = UniRutaIcons.QrCode,
                tone = StatusTone.POSITIVE,
                onClick = onOpenPass
            )
        ),
        onLogout = onLogout,
        modifier = modifier,
        footer = {
            NotificationPermissionPrompt(
                purpose = "UniRuta te avisará cuando crees una reserva, envíes un comprobante o tengas un pase disponible.",
                modifier = Modifier.padding(top = 4.dp)
            )
            SectionLabel(text = "Seguridad", modifier = Modifier.padding(top = 12.dp))
            EmergencyContactCard(
                state = contactState,
                onPickContact = onPickContact,
                onCall = onCallContact,
                onRemove = onRemoveContact,
                onDismissError = onDismissContactError
            )
        }
    )
}
