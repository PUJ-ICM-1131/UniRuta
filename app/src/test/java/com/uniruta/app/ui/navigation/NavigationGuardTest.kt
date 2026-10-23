package com.uniruta.app.ui.navigation

import com.uniruta.app.data.mock.MockAdminData
import com.uniruta.app.data.mock.MockDriverData
import com.uniruta.app.data.mock.MockStudentData
import com.uniruta.app.data.mock.MockTrips
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.model.UserRole
import com.uniruta.app.notifications.NotificationDestination
import com.uniruta.app.notifications.NotificationTarget
import com.uniruta.app.viewmodel.AdminUiState
import com.uniruta.app.viewmodel.DriverUiState
import com.uniruta.app.viewmodel.StudentUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationGuardTest {

    private val student = StudentUiState(
        trips = MockTrips.all,
        reservations = MockStudentData.reservations,
        payments = MockStudentData.payments,
        passes = MockStudentData.passes
    )
    private val driver = DriverUiState(trips = MockDriverData.assignedTrips, passengers = MockDriverData.passengers)
    private val admin = AdminUiState(
        routes = MockAdminData.routes,
        vehicles = MockAdminData.vehicles,
        trips = MockAdminData.trips,
        passengers = MockAdminData.passengers
    )

    @Test
    fun everyProtectedDestinationBelongsToExactlyOneRole() {
        assertNull(Login.requiredRole())
        assertEquals(UserRole.STUDENT, StudentPass("res-2002").requiredRole())
        assertEquals(UserRole.STUDENT, TransferPayment("res-2001").requiredRole())
        assertEquals(UserRole.DRIVER, ActiveTrip.requiredRole())
        assertEquals(UserRole.DRIVER, TripFinished("trip-001").requiredRole())
        assertEquals(UserRole.ADMIN, AdminPaymentDetail("pay-3001").requiredRole())
        assertEquals(UserRole.ADMIN, AdminRouteForm().requiredRole())
    }

    @Test
    fun backStacksAreOnlyValidForTheirOwnRole() {
        val studentStack = listOf(StudentHome, StudentTrips, TripDetail("trip-001"))

        assertTrue(studentStack.isValidFor(UserRole.STUDENT))
        assertFalse(studentStack.isValidFor(UserRole.ADMIN))
        assertFalse(studentStack.isValidFor(null))
        assertFalse(listOf(StudentHome, AdminPayments).isValidFor(UserRole.STUDENT))
        assertFalse(listOf(StudentTrips).isValidFor(UserRole.STUDENT))
        assertTrue(listOf(Login).isValidFor(null))
        assertFalse(listOf(Login).isValidFor(UserRole.DRIVER))
    }

    @Test
    fun notificationTargetsRebuildAStackWithoutDuplicates() {
        val detail = NotificationTarget(NotificationDestination.RESERVATION_DETAIL, "res-2001")
            .backStackFor(student, driver, admin)
        assertEquals(listOf(StudentHome, StudentReservations, ReservationDetail("res-2001")), detail)
        assertTrue(detail.isValidFor(UserRole.STUDENT))

        val payment = NotificationTarget(NotificationDestination.ADMIN_PAYMENT_DETAIL, "pay-3001")
            .backStackFor(student, driver, admin)
        assertEquals(listOf(AdminHome, AdminPayments, AdminPaymentDetail("pay-3001")), payment)
        assertTrue(payment.isValidFor(UserRole.ADMIN))
    }

    @Test
    fun staleNotificationTargetsFallBackToSafeScreens() {
        assertEquals(
            listOf(StudentHome, StudentReservations),
            NotificationTarget(NotificationDestination.RESERVATION_DETAIL, "res-inexistente")
                .backStackFor(student, driver, admin)
        )
        assertEquals(
            listOf(DriverHome, DriverTrips),
            NotificationTarget(NotificationDestination.ACTIVE_TRIP, "trip-001").backStackFor(student, driver, admin)
        )
        assertEquals(
            listOf(DriverHome, DriverTrips),
            NotificationTarget(NotificationDestination.TRIP_FINISHED, "trip-001").backStackFor(student, driver, admin)
        )
        assertEquals(
            listOf(StudentHome, StudentPass(null)),
            NotificationTarget(NotificationDestination.STUDENT_PASS, "res-2001").backStackFor(student, driver, admin)
        )
    }

    @Test
    fun driverTargetsOpenTheLiveTripWhenItExists() {
        val running = driver.copy(
            activeTripId = "trip-001",
            trips = driver.trips.map { if (it.id == "trip-001") it.copy(status = TripStatus.IN_PROGRESS) else it }
        )
        assertEquals(
            listOf(DriverHome, ActiveTrip),
            NotificationTarget(NotificationDestination.ACTIVE_TRIP, "trip-001").backStackFor(student, running, admin)
        )

        val finished = driver.copy(
            trips = driver.trips.map { if (it.id == "trip-001") it.copy(status = TripStatus.COMPLETED) else it }
        )
        assertEquals(
            listOf(DriverHome, TripFinished("trip-001")),
            NotificationTarget(NotificationDestination.TRIP_FINISHED, "trip-001").backStackFor(student, finished, admin)
        )
    }
}
