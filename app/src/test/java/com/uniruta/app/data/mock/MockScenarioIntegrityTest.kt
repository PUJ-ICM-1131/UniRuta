package com.uniruta.app.data.mock

import com.uniruta.app.data.model.PassStatus
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.model.VehicleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockScenarioIntegrityTest {

    private val allPassengers = (MockDriverData.passengers + MockAdminData.passengers).distinctBy { it.reservation.id }
    private val allReservations = (MockStudentData.reservations + allPassengers.map { it.reservation }).distinctBy { it.id }
    private val allPayments = (MockStudentData.payments + allPassengers.map { it.payment }).distinctBy { it.id }

    @Test
    fun identifiersAreUnique() {
        assertEquals(MockTrips.all.size, MockTrips.all.map { it.id }.toSet().size)
        assertEquals(MockAccounts.all.size, MockAccounts.all.map { it.id }.toSet().size)
        assertEquals(MockAdminData.vehicles.size, MockAdminData.vehicles.map { it.plate }.toSet().size)
        val passes = MockStudentData.passes + allPassengers.mapNotNull { it.pass }
        assertEquals(passes.distinctBy { it.id }.size, passes.map { it.token }.toSet().size)
        val stopIds = MockAdminData.routes.flatMap { route -> route.stops.map { it.id } }
        assertEquals(stopIds.size, stopIds.toSet().size)
    }

    @Test
    fun everyRecordPointsToAnExistingParent() {
        val tripIds = MockTrips.all.map { it.id }.toSet()
        val reservationIds = allReservations.map { it.id }.toSet()
        val routeIds = MockAdminData.routes.map { it.id }.toSet()

        assertTrue(allReservations.all { it.tripId in tripIds })
        assertTrue(allPayments.all { it.reservationId in reservationIds })
        assertTrue(MockStudentData.passes.all { it.reservationId in reservationIds })
        assertTrue(MockTrips.all.all { it.route.id in routeIds })
        assertTrue(MockDriverData.assignedTrips.all { it.driverName == MockDriverData.DRIVER_NAME })
        assertTrue(MockTrips.all.map { it.driverName }.all { it in MockAdminData.drivers })
        assertTrue(MockTrips.all.map { it.vehiclePlate }.all { plate -> MockAdminData.vehicles.any { it.plate == plate } })
    }

    @Test
    fun paymentsMatchTheirReservationsAndTripPrices() {
        allPayments.forEach { payment ->
            val reservation = allReservations.first { it.id == payment.reservationId }
            val trip = MockTrips.all.first { it.id == reservation.tripId }
            assertEquals(trip.price, payment.amount)
            assertEquals(reservation.selectedPaymentMethod, payment.method)
        }
    }

    @Test
    fun seatCountsAreConsistent() {
        assertTrue(MockTrips.all.all { it.availableSeats in 0..it.totalSeats })
        assertTrue(MockTrips.all.any { it.isBookable })
        assertTrue(MockTrips.all.any { it.status == TripStatus.SCHEDULED && it.availableSeats == 0 })
    }

    @Test
    fun driverTripOccupancyMatchesThePassengerList() {
        val driverTripIds = MockDriverData.assignedTrips.map { it.id }.toSet()
        MockDriverData.assignedTrips.forEach { trip ->
            val activePassengers = MockDriverData.passengers.filter { it.tripId == trip.id && it.reservation.isActive }
            assertEquals(trip.totalSeats - trip.availableSeats, activePassengers.size)
        }
        allReservations
            .filter { it.isActive && it.tripId in driverTripIds }
            .forEach { reservation ->
                assertTrue(MockDriverData.passengers.any { it.reservation == reservation })
            }
    }

    @Test
    fun scheduledTripsUseAvailableVehiclesWithoutOverlaps() {
        val scheduled = MockTrips.all.filter { it.status == TripStatus.SCHEDULED }
        scheduled.forEach { trip ->
            val vehicle = MockAdminData.vehicles.first { it.plate == trip.vehiclePlate }
            assertEquals(VehicleStatus.ACTIVE, vehicle.status)
            assertTrue(trip.totalSeats <= vehicle.capacity)
        }
        val slots = scheduled.map { Triple(it.vehiclePlate, it.date, it.departureTime) }
        assertEquals(slots.size, slots.toSet().size)
    }

    @Test
    fun studentPassIsTheSameTokenTheDriverValidates() {
        val pass = MockStudentData.passes.single()
        val driverPassenger = MockDriverData.passengers.first { it.pass?.token == pass.token }

        assertEquals(pass, driverPassenger.pass)
        assertEquals(pass.reservationId, driverPassenger.reservation.id)
        assertEquals(MockAccounts.all.first().name, driverPassenger.studentName)
        assertTrue(MockDriverData.assignedTrips.any { it.id == driverPassenger.tripId })
        assertEquals(pass.token, MockDriverData.demoScans.first().token)
    }

    @Test
    fun adminSeesTheSameStudentRecords() {
        MockStudentData.reservations.forEach { reservation ->
            val entry = MockAdminData.passengers.first { it.reservation.id == reservation.id }
            assertEquals(reservation, entry.reservation)
            assertEquals(MockStudentData.payments.first { it.reservationId == reservation.id }, entry.payment)
        }
    }

    @Test
    fun scenariosCoverEveryStateNeededForTheDemonstration() {
        val reservationStates = MockStudentData.reservations.map { it.status }.toSet()
        assertTrue(reservationStates.containsAll(setOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED)))
        val paymentStates = MockStudentData.payments.map { it.status }.toSet()
        assertTrue(paymentStates.containsAll(setOf(PaymentStatus.UNDER_REVIEW, PaymentStatus.CONFIRMED)))

        val driverTrip = MockDriverData.passengers.filter { it.tripId == "trip-001" }
        assertTrue(driverTrip.any { it.hasBoarded })
        assertTrue(driverTrip.any { it.canRegisterBoarding })
        assertTrue(driverTrip.any { it.awaitsCashOnBoarding })
        assertTrue(MockDriverData.passengers.any { it.pass?.status == PassStatus.USED })
        assertTrue(MockDriverData.passengers.any { it.tripId != "trip-001" && it.pass?.status == PassStatus.ACTIVE })

        assertTrue(MockAdminData.passengers.any { it.requiresAdminReview && it.payment.method == PaymentMethod.TRANSFER })
        assertTrue(MockAdminData.passengers.any { it.requiresAdminReview && it.payment.method == PaymentMethod.CASH_POINT })
        assertTrue(MockAdminData.passengers.any { !it.reviewableByAdmin && it.payment.method == PaymentMethod.CASH_BOARDING })
    }
}
