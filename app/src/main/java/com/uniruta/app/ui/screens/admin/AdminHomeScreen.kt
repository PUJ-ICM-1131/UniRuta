package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.ui.components.FeatureItem
import com.uniruta.app.ui.components.Metric
import com.uniruta.app.ui.components.MetricGrid
import com.uniruta.app.ui.components.RoleHomeScaffold
import com.uniruta.app.ui.components.SectionLabel
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.permissions.NotificationPermissionPrompt
import com.uniruta.app.ui.theme.UniRutaIcons

@Composable
fun AdminHomeScreen(
    user: SessionUser,
    activeRouteCount: Int,
    activeVehicleCount: Int,
    scheduledTripCount: Int,
    pendingReviewCount: Int,
    onOpenRoutes: () -> Unit,
    onOpenVehicles: () -> Unit,
    onOpenTrips: () -> Unit,
    onOpenPayments: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoleHomeScaffold(
        user = user,
        headline = "Supervisa la operación del servicio de rutas.",
        sectionLabel = "Gestión",
        features = listOf(
            FeatureItem(
                title = "Rutas",
                description = "Define los trayectos y paradas del servicio.",
                icon = UniRutaIcons.Route,
                tone = StatusTone.INFO,
                onClick = onOpenRoutes
            ),
            FeatureItem(
                title = "Vehículos",
                description = "Administra la flota y su capacidad.",
                icon = UniRutaIcons.Truck,
                tone = StatusTone.POSITIVE,
                onClick = onOpenVehicles
            ),
            FeatureItem(
                title = "Recorridos",
                description = "Programa horarios y asigna conductores.",
                icon = UniRutaIcons.Calendar,
                tone = StatusTone.INFO,
                onClick = onOpenTrips
            ),
            FeatureItem(
                title = "Pagos",
                description = "Supervisa los pagos y su estado.",
                icon = UniRutaIcons.Cash,
                tone = StatusTone.WARNING,
                onClick = onOpenPayments
            )
        ),
        onLogout = onLogout,
        modifier = modifier,
        header = {
            SectionLabel(text = "Resumen")
            MetricGrid(
                metrics = listOf(
                    Metric(label = "Rutas activas", value = activeRouteCount, icon = UniRutaIcons.Route),
                    Metric(label = "Vehículos activos", value = activeVehicleCount, icon = UniRutaIcons.Truck),
                    Metric(label = "Recorridos programados", value = scheduledTripCount, icon = UniRutaIcons.Calendar),
                    Metric(
                        label = "Pagos por revisar",
                        value = pendingReviewCount,
                        icon = UniRutaIcons.Cash,
                        highlighted = pendingReviewCount > 0
                    )
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        footer = {
            NotificationPermissionPrompt(
                purpose = "UniRuta te avisará cuando apruebes o rechaces un pago.",
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    )
}
