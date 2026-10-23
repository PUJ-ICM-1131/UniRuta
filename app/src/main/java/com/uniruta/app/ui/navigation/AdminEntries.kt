package com.uniruta.app.ui.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.uniruta.app.data.mock.MockAdminData
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.ui.screens.admin.AdminHomeScreen
import com.uniruta.app.ui.screens.admin.AdminPaymentDetailScreen
import com.uniruta.app.ui.screens.admin.AdminPaymentsScreen
import com.uniruta.app.ui.screens.admin.AdminRouteFormScreen
import com.uniruta.app.ui.screens.admin.AdminRoutesScreen
import com.uniruta.app.ui.screens.admin.AdminTripFormScreen
import com.uniruta.app.ui.screens.admin.AdminTripsScreen
import com.uniruta.app.ui.screens.admin.AdminVehicleFormScreen
import com.uniruta.app.ui.screens.admin.AdminVehiclesScreen
import com.uniruta.app.viewmodel.AdminUiState
import com.uniruta.app.viewmodel.AdminViewModel

fun EntryProviderScope<NavKey>.adminEntries(
    uiState: State<AdminUiState>,
    viewModel: AdminViewModel,
    user: SessionUser?,
    onLogout: () -> Unit,
    backStack: NavBackStack<NavKey>
) {
    val state by uiState

    entry<AdminHome> {
        user?.let { current ->
            AdminHomeScreen(
                user = current,
                activeRouteCount = state.activeRoutes.size,
                activeVehicleCount = state.activeVehicles.size,
                scheduledTripCount = state.scheduledTripCount,
                pendingReviewCount = state.pendingReviewCount,
                onOpenRoutes = { backStack.add(AdminRoutes) },
                onOpenVehicles = { backStack.add(AdminVehicles) },
                onOpenTrips = { backStack.add(AdminTrips) },
                onOpenPayments = { backStack.add(AdminPayments) },
                onLogout = onLogout
            )
        }
    }

    entry<AdminRoutes> {
        AdminRoutesScreen(
            routes = state.routes,
            onRouteSelected = { backStack.add(AdminRouteForm(it)) },
            onCreateRoute = { backStack.add(AdminRouteForm()) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminRouteForm> { key ->
        AdminRouteFormScreen(
            route = state.routeBy(key.routeId),
            onSave = { name, origin, destination, stops ->
                viewModel.saveRoute(key.routeId, name, origin, destination, stops)
            },
            onDeactivate = { key.routeId?.let(viewModel::deactivateRoute) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminVehicles> {
        AdminVehiclesScreen(
            vehicles = state.vehicles,
            onVehicleSelected = { backStack.add(AdminVehicleForm(it)) },
            onCreateVehicle = { backStack.add(AdminVehicleForm()) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminVehicleForm> { key ->
        AdminVehicleFormScreen(
            vehicle = state.vehicleBy(key.vehicleId),
            onSave = { plate, capacity, status ->
                viewModel.saveVehicle(key.vehicleId, plate, capacity, status)
            },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminTrips> {
        AdminTripsScreen(
            trips = state.orderedTrips,
            onScheduleTrip = { backStack.add(AdminTripForm) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminTripForm> {
        AdminTripFormScreen(
            routes = state.activeRoutes,
            vehicles = state.activeVehicles,
            drivers = MockAdminData.drivers,
            onSchedule = { routeId, date, time, vehicleId, driver, seats, price ->
                viewModel.scheduleTrip(routeId, date, time, vehicleId, driver, seats, price)
            },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminPayments> {
        AdminPaymentsScreen(
            entries = state.orderedPayments,
            tripResolver = state::tripBy,
            onPaymentSelected = { backStack.add(AdminPaymentDetail(it)) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<AdminPaymentDetail> { key ->
        state.paymentEntryBy(key.paymentId)?.let { paymentEntry ->
            AdminPaymentDetailScreen(
                entry = paymentEntry,
                trip = state.tripBy(paymentEntry.tripId),
                onApprove = { viewModel.approvePayment(key.paymentId) },
                onReject = { reason -> viewModel.rejectPayment(key.paymentId, reason) },
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }
}
