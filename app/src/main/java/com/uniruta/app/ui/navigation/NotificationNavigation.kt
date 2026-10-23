package com.uniruta.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.notifications.NotificationDestination
import com.uniruta.app.notifications.NotificationTarget
import com.uniruta.app.viewmodel.AdminUiState
import com.uniruta.app.viewmodel.DriverUiState
import com.uniruta.app.viewmodel.StudentUiState

fun NotificationTarget.backStackFor(
    student: StudentUiState,
    driver: DriverUiState,
    admin: AdminUiState
): List<NavKey> {
    val id = entityId
    return when (destination) {
        NotificationDestination.STUDENT_RESERVATIONS -> listOf(StudentHome, StudentReservations)
        NotificationDestination.RESERVATION_DETAIL ->
            if (id != null && student.reservationBy(id) != null) {
                listOf(StudentHome, StudentReservations, ReservationDetail(id))
            } else {
                listOf(StudentHome, StudentReservations)
            }
        NotificationDestination.STUDENT_PASS ->
            listOf(StudentHome, StudentPass(id?.takeIf { student.availablePassFor(it) != null }))
        NotificationDestination.DRIVER_HOME -> listOf(DriverHome)
        NotificationDestination.ACTIVE_TRIP ->
            if (driver.activeTrip != null) listOf(DriverHome, ActiveTrip) else listOf(DriverHome, DriverTrips)
        NotificationDestination.TRIP_FINISHED ->
            if (id != null && driver.tripBy(id)?.status == TripStatus.COMPLETED) {
                listOf(DriverHome, TripFinished(id))
            } else {
                listOf(DriverHome, DriverTrips)
            }
        NotificationDestination.ADMIN_PAYMENTS -> listOf(AdminHome, AdminPayments)
        NotificationDestination.ADMIN_PAYMENT_DETAIL ->
            if (id != null && admin.paymentEntryBy(id) != null) {
                listOf(AdminHome, AdminPayments, AdminPaymentDetail(id))
            } else {
                listOf(AdminHome, AdminPayments)
            }
    }
}
