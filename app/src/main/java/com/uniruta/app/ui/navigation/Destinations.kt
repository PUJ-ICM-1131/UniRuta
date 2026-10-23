package com.uniruta.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.uniruta.app.data.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data object Login : NavKey

@Serializable
data object StudentHome : NavKey

@Serializable
data object DriverHome : NavKey

@Serializable
data object AdminHome : NavKey

@Serializable
data object StudentTrips : NavKey

@Serializable
data class TripDetail(val tripId: String) : NavKey

@Serializable
data class ConfirmReservation(val tripId: String) : NavKey

@Serializable
data class PaymentMethodChoice(val reservationId: String) : NavKey

@Serializable
data class TransferPayment(val reservationId: String) : NavKey

@Serializable
data class PaymentOutcome(val reservationId: String) : NavKey

@Serializable
data object StudentReservations : NavKey

@Serializable
data class ReservationDetail(val reservationId: String) : NavKey

@Serializable
data class StudentPass(val reservationId: String? = null) : NavKey

@Serializable
data object DriverTrips : NavKey

@Serializable
data class DriverTripDetail(val tripId: String) : NavKey

@Serializable
data object ActiveTrip : NavKey

@Serializable
data object QrScannerMock : NavKey

@Serializable
data object BoardingResult : NavKey

@Serializable
data object DriverPassengers : NavKey

@Serializable
data object ReportIncident : NavKey

@Serializable
data class TripFinished(val tripId: String) : NavKey

@Serializable
data object AdminRoutes : NavKey

@Serializable
data class AdminRouteForm(val routeId: String? = null) : NavKey

@Serializable
data object AdminVehicles : NavKey

@Serializable
data class AdminVehicleForm(val vehicleId: String? = null) : NavKey

@Serializable
data object AdminTrips : NavKey

@Serializable
data object AdminTripForm : NavKey

@Serializable
data object AdminPayments : NavKey

@Serializable
data class AdminPaymentDetail(val paymentId: String) : NavKey

fun homeDestinationFor(role: UserRole): NavKey = when (role) {
    UserRole.STUDENT -> StudentHome
    UserRole.DRIVER -> DriverHome
    UserRole.ADMIN -> AdminHome
}

fun NavKey.requiredRole(): UserRole? = when (this) {
    StudentHome, StudentTrips, StudentReservations,
    is TripDetail, is ConfirmReservation, is PaymentMethodChoice, is TransferPayment,
    is PaymentOutcome, is ReservationDetail, is StudentPass -> UserRole.STUDENT
    DriverHome, DriverTrips, ActiveTrip, QrScannerMock, BoardingResult, DriverPassengers, ReportIncident,
    is DriverTripDetail, is TripFinished -> UserRole.DRIVER
    AdminHome, AdminRoutes, AdminVehicles, AdminTrips, AdminTripForm, AdminPayments,
    is AdminRouteForm, is AdminVehicleForm, is AdminPaymentDetail -> UserRole.ADMIN
    else -> null
}

fun List<NavKey>.isValidFor(role: UserRole?): Boolean = when (role) {
    null -> size == 1 && first() == Login
    else -> firstOrNull() == homeDestinationFor(role) && all { it.requiredRole() == role }
}
