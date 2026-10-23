package com.uniruta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uniruta.app.UniRutaApplication
import com.uniruta.app.data.mock.MockAdminData
import com.uniruta.app.data.model.DigitalPass
import com.uniruta.app.data.model.PassStatus
import com.uniruta.app.data.model.Payment
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.RejectionReason
import com.uniruta.app.data.model.Reservation
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.Stop
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.model.Vehicle
import com.uniruta.app.data.model.VehicleStatus
import com.uniruta.app.notifications.AppNotifier
import com.uniruta.app.notifications.NotificationMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

private const val DEFAULT_DURATION_MINUTES = 45
private const val DEFAULT_DISTANCE_KM = 15.0

data class AdminUiState(
    val routes: List<Route> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val trips: List<Trip> = emptyList(),
    val passengers: List<TripPassenger> = emptyList()
) {
    val reservations: List<Reservation>
        get() = passengers.map { it.reservation }

    val payments: List<Payment>
        get() = passengers.map { it.payment }

    val digitalPasses: List<DigitalPass>
        get() = passengers.mapNotNull { it.pass }

    val activeRoutes: List<Route>
        get() = routes.filter { it.active }

    val activeVehicles: List<Vehicle>
        get() = vehicles.filter { it.status == VehicleStatus.ACTIVE }

    val scheduledTripCount: Int
        get() = trips.count { it.status == TripStatus.SCHEDULED }

    val pendingReviewCount: Int
        get() = passengers.count { it.requiresAdminReview }

    val orderedPayments: List<TripPassenger>
        get() = passengers.sortedByDescending { it.requiresAdminReview }

    val orderedTrips: List<Trip>
        get() = trips.sortedWith(compareBy({ it.date }, { it.departureTime }))

    fun routeBy(id: String?): Route? = routes.firstOrNull { it.id == id }

    fun vehicleBy(id: String?): Vehicle? = vehicles.firstOrNull { it.id == id }

    fun tripBy(id: String): Trip? = trips.firstOrNull { it.id == id }

    val hasScenarioChanges: Boolean
        get() = routes != MockAdminData.routes ||
            vehicles != MockAdminData.vehicles ||
            trips != MockAdminData.trips ||
            passengers != MockAdminData.passengers

    fun paymentEntryBy(paymentId: String?): TripPassenger? =
        passengers.firstOrNull { it.payment.id == paymentId }
}

class AdminViewModel(
    private val notifier: AppNotifier = AppNotifier.None
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var routeSequence = 0
    private var vehicleSequence = 0
    private var tripSequence = 0
    private var passSequence = 0

    fun saveRoute(
        routeId: String?,
        name: String,
        origin: String,
        destination: String,
        stopNames: List<String>
    ): String? {
        val cleanStops = stopNames.map { it.trim() }.filter { it.isNotEmpty() }
        when {
            name.isBlank() -> return "El nombre de la ruta es obligatorio."
            origin.isBlank() -> return "El origen es obligatorio."
            destination.isBlank() -> return "El destino es obligatorio."
            cleanStops.size < 2 -> return "La ruta debe tener al menos dos paradas."
        }

        val existing = _uiState.value.routeBy(routeId)
        val id = existing?.id ?: "route-mock-${++routeSequence + 100}"
        val stops = cleanStops.mapIndexed { index, stopName ->
            Stop(
                id = "$id-stop-${index + 1}",
                name = stopName,
                order = index + 1,
                location = existing?.stops
                    ?.firstOrNull { it.name.equals(stopName, ignoreCase = true) }
                    ?.location
            )
        }
        val route = Route(
            id = id,
            name = name.trim(),
            origin = origin.trim(),
            destination = destination.trim(),
            stops = stops,
            active = existing?.active ?: true
        )

        _uiState.update { current ->
            current.copy(
                routes = if (existing == null) {
                    current.routes + route
                } else {
                    current.routes.map { if (it.id == id) route else it }
                },
                trips = current.trips.map { if (it.route.id == id) it.copy(route = route) else it }
            )
        }
        return null
    }

    fun deactivateRoute(routeId: String) {
        _uiState.update { current ->
            current.copy(
                routes = current.routes.map {
                    if (it.id == routeId) it.copy(active = false) else it
                }
            )
        }
    }

    fun saveVehicle(
        vehicleId: String?,
        plate: String,
        capacity: String,
        status: VehicleStatus
    ): String? {
        val cleanPlate = plate.trim().uppercase()
        val seats = capacity.trim().toIntOrNull()
        val state = _uiState.value
        when {
            cleanPlate.isBlank() -> return "La placa es obligatoria."
            seats == null || seats <= 0 -> return "La capacidad debe ser mayor que cero."
            state.vehicles.any { it.plate == cleanPlate && it.id != vehicleId } ->
                return "Ya existe un vehículo con esa placa."
        }

        val existing = state.vehicleBy(vehicleId)
        val vehicle = Vehicle(
            id = existing?.id ?: "veh-mock-${++vehicleSequence + 100}",
            plate = cleanPlate,
            capacity = seats ?: 0,
            status = status
        )

        _uiState.update { current ->
            current.copy(
                vehicles = if (existing == null) {
                    current.vehicles + vehicle
                } else {
                    current.vehicles.map { if (it.id == vehicle.id) vehicle else it }
                }
            )
        }
        return null
    }

    fun scheduleTrip(
        routeId: String?,
        date: LocalDate?,
        departureTime: LocalTime?,
        vehicleId: String?,
        driverName: String?,
        totalSeats: String,
        price: String
    ): String? {
        val state = _uiState.value
        val route = state.routeBy(routeId)
        val vehicle = state.vehicleBy(vehicleId)
        val seats = totalSeats.trim().toIntOrNull()
        val seatPrice = price.trim().toIntOrNull()

        when {
            route == null || !route.active -> return "Selecciona una ruta activa."
            date == null -> return "La fecha no es válida. Usa el formato dd/MM/aaaa."
            departureTime == null -> return "La hora no es válida. Usa el formato HH:mm."
            vehicle == null || vehicle.status != VehicleStatus.ACTIVE ->
                return "Selecciona un vehículo activo."
            driverName.isNullOrBlank() -> return "Selecciona un conductor."
            seats == null || seats <= 0 -> return "Los cupos deben ser mayores que cero."
            seats > vehicle.capacity ->
                return "Los cupos no pueden superar la capacidad del vehículo (${vehicle.capacity})."
            seatPrice == null || seatPrice <= 0 -> return "El precio debe ser mayor que cero."
        }

        val reference = state.trips.firstOrNull { it.route.id == route!!.id }
        val trip = Trip(
            id = "trip-mock-${++tripSequence + 100}",
            route = route!!,
            date = date!!,
            departureTime = departureTime!!,
            availableSeats = seats!!,
            totalSeats = seats,
            vehiclePlate = vehicle!!.plate,
            driverName = driverName!!,
            estimatedDurationMinutes = reference?.estimatedDurationMinutes ?: DEFAULT_DURATION_MINUTES,
            estimatedDistanceKm = reference?.estimatedDistanceKm ?: DEFAULT_DISTANCE_KM,
            price = seatPrice!!,
            status = TripStatus.SCHEDULED
        )

        _uiState.update { it.copy(trips = it.trips + trip) }
        return null
    }

    fun approvePayment(paymentId: String) {
        val entry = _uiState.value.paymentEntryBy(paymentId) ?: return
        if (!entry.reviewableByAdmin) return
        if (entry.payment.status == PaymentStatus.CONFIRMED) return

        val pass = entry.pass ?: DigitalPass(
            id = "pass-mock-${++passSequence + 100}",
            reservationId = entry.reservation.id,
            token = UUID.randomUUID().toString(),
            status = PassStatus.ACTIVE
        )

        updateEntry(paymentId) {
            it.copy(
                reservation = it.reservation.copy(status = ReservationStatus.CONFIRMED),
                payment = it.payment.copy(
                    status = PaymentStatus.CONFIRMED,
                    rejectionReason = null
                ),
                pass = pass
            )
        }
        notifier.notify(
            NotificationMessages.paymentApproved(
                paymentId = paymentId,
                reservationId = entry.reservation.id,
                trip = _uiState.value.tripBy(entry.tripId)
            )
        )
    }

    fun rejectPayment(paymentId: String, reason: RejectionReason) {
        val entry = _uiState.value.paymentEntryBy(paymentId) ?: return
        if (!entry.reviewableByAdmin) return
        if (entry.payment.method != PaymentMethod.TRANSFER) return

        updateEntry(paymentId) {
            it.copy(
                reservation = it.reservation.copy(status = ReservationStatus.PENDING),
                payment = it.payment.copy(
                    status = PaymentStatus.REJECTED,
                    rejectionReason = reason
                )
            )
        }
        notifier.notify(NotificationMessages.paymentRejected(paymentId, entry.reservation.id, reason))
    }

    fun reset() {
        routeSequence = 0
        vehicleSequence = 0
        tripSequence = 0
        passSequence = 0
        _uiState.value = initialState()
    }

    private fun updateEntry(paymentId: String, transform: (TripPassenger) -> TripPassenger) {
        _uiState.update { current ->
            current.copy(
                passengers = current.passengers.map {
                    if (it.payment.id == paymentId) transform(it) else it
                }
            )
        }
    }

    private fun initialState() = AdminUiState(
        routes = MockAdminData.routes,
        vehicles = MockAdminData.vehicles,
        trips = MockAdminData.trips,
        passengers = MockAdminData.passengers
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as UniRutaApplication
                AdminViewModel(notifier = application.container.notifier)
            }
        }
    }
}
