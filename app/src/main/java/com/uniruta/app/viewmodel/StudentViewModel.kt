package com.uniruta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uniruta.app.UniRutaApplication
import com.uniruta.app.data.geo.pointAtFraction
import com.uniruta.app.data.mock.MockStudentData
import com.uniruta.app.data.mock.MockTrips
import com.uniruta.app.data.model.DigitalPass
import com.uniruta.app.data.model.PassStatus
import com.uniruta.app.data.model.Payment
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.PositionSource
import com.uniruta.app.data.model.Reservation
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.VehiclePosition
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.data.routing.RoutePlanner
import com.uniruta.app.data.storage.ImageResult
import com.uniruta.app.data.storage.ImageStore
import com.uniruta.app.data.storage.RECEIPTS_FOLDER
import com.uniruta.app.notifications.AppNotifier
import com.uniruta.app.notifications.NotificationMessages
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

private const val DEMO_VEHICLE_TICK_MILLIS = 1_500L
private const val DEMO_VEHICLE_LOOP_MILLIS = 120_000L

data class StudentUiState(
    val trips: List<Trip> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val passes: List<DigitalPass> = emptyList(),
    val routePaths: Map<String, RoutePathState> = emptyMap(),
    val demoVehiclePositions: Map<String, VehiclePosition> = emptyMap(),
    val receiptImportInProgress: Boolean = false,
    val receiptError: String? = null
) {
    val orderedReservations: List<Reservation>
        get() = reservations.sortedByDescending { it.reservationDate }

    fun tripBy(id: String): Trip? = trips.firstOrNull { it.id == id }

    fun routePathFor(trip: Trip): RoutePathState? = routePaths[trip.route.id]

    fun reservationBy(id: String): Reservation? = reservations.firstOrNull { it.id == id }

    fun paymentFor(reservationId: String): Payment? =
        payments.firstOrNull { it.reservationId == reservationId }

    fun hasActiveReservationFor(tripId: String): Boolean =
        reservations.any { it.tripId == tripId && it.isActive }

    val availablePassReservationIds: Set<String>
        get() = passes.mapNotNull { pass -> availablePassFor(pass.reservationId)?.reservationId }.toSet()

    val hasScenarioChanges: Boolean
        get() = trips != MockTrips.all ||
            reservations != MockStudentData.reservations ||
            payments != MockStudentData.payments ||
            passes != MockStudentData.passes

    fun availablePassFor(reservationId: String?): DigitalPass? {
        val candidates = if (reservationId == null) passes else passes.filter { it.reservationId == reservationId }
        return candidates.firstOrNull { pass ->
            pass.status == PassStatus.ACTIVE &&
                reservationBy(pass.reservationId)?.status == ReservationStatus.CONFIRMED &&
                paymentFor(pass.reservationId)?.status == PaymentStatus.CONFIRMED
        }
    }
}

class StudentViewModel(
    private val routePlanner: RoutePlanner,
    private val imageStore: ImageStore,
    private val notifier: AppNotifier = AppNotifier.None
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private var reservationSequence = 0
    private var paymentSequence = 0
    private var demoVehicleJob: Job? = null
    private var knownPassReservationIds = _uiState.value.availablePassReservationIds

    init {
        viewModelScope.launch { imageStore.clear(RECEIPTS_FOLDER) }
    }

    fun reserveSeat(tripId: String): String? {
        val state = _uiState.value
        state.reservations.firstOrNull { it.tripId == tripId && it.isActive }?.let { return it.id }

        val trip = state.tripBy(tripId) ?: return null
        if (!trip.isBookable) return null

        val reservation = Reservation(
            id = "res-${++reservationSequence + 9000}",
            tripId = tripId,
            reservationDate = LocalDateTime.now(),
            status = ReservationStatus.PENDING
        )

        _uiState.update { current ->
            current.copy(
                trips = current.trips.map {
                    if (it.id == tripId) it.copy(availableSeats = (it.availableSeats - 1).coerceAtLeast(0)) else it
                },
                reservations = current.reservations + reservation
            )
        }
        notifier.notify(NotificationMessages.reservationCreated(reservation.id, trip))
        return reservation.id
    }

    fun selectPaymentMethod(reservationId: String, method: PaymentMethod) {
        val state = _uiState.value
        val reservation = state.reservationBy(reservationId) ?: return
        val amount = state.tripBy(reservation.tripId)?.price ?: return
        val existing = state.paymentFor(reservationId)
        existing?.receiptPath?.let { path -> viewModelScope.launch { imageStore.delete(path) } }

        val payment = existing?.copy(
            method = method,
            amount = amount,
            status = PaymentStatus.PENDING,
            receiptName = null,
            receiptPath = null
        ) ?: Payment(
            id = "pay-${++paymentSequence + 9000}",
            reservationId = reservationId,
            amount = amount,
            method = method,
            status = PaymentStatus.PENDING
        )

        _uiState.update { current ->
            current.copy(
                reservations = current.reservations.map {
                    if (it.id == reservationId) it.copy(selectedPaymentMethod = method) else it
                },
                payments = current.payments.filterNot { it.reservationId == reservationId } + payment,
                receiptError = null
            )
        }
        announceNewPasses()
    }

    fun attachReceipt(reservationId: String, sourceUri: String) {
        val payment = _uiState.value.paymentFor(reservationId) ?: return
        if (_uiState.value.receiptImportInProgress) return

        _uiState.update { it.copy(receiptImportInProgress = true, receiptError = null) }
        viewModelScope.launch {
            when (val result = imageStore.importImage(sourceUri, RECEIPTS_FOLDER, "comprobante_${payment.id}")) {
                is ImageResult.Success -> {
                    updatePayment(reservationId) {
                        it.copy(receiptName = result.image.displayName, receiptPath = result.image.path)
                    }
                    _uiState.update { it.copy(receiptImportInProgress = false) }
                }
                is ImageResult.Failure -> _uiState.update {
                    it.copy(receiptImportInProgress = false, receiptError = result.message)
                }
            }
        }
    }

    fun clearReceiptError() {
        _uiState.update { it.copy(receiptError = null) }
    }

    fun submitReceipt(reservationId: String) {
        val payment = _uiState.value.paymentFor(reservationId) ?: return
        if (payment.receiptName == null || payment.status == PaymentStatus.UNDER_REVIEW) return
        updatePayment(reservationId) { it.copy(status = PaymentStatus.UNDER_REVIEW) }
        val trip = _uiState.value.reservationBy(reservationId)?.let { _uiState.value.tripBy(it.tripId) }
        notifier.notify(NotificationMessages.receiptSubmitted(reservationId, trip))
    }

    fun cancelReservation(reservationId: String) {
        val reservation = _uiState.value.reservationBy(reservationId) ?: return
        if (!reservation.isActive) return

        _uiState.update { current ->
            current.copy(
                trips = current.trips.map {
                    if (it.id == reservation.tripId) {
                        it.copy(availableSeats = (it.availableSeats + 1).coerceAtMost(it.totalSeats))
                    } else {
                        it
                    }
                },
                reservations = current.reservations.map {
                    if (it.id == reservationId) it.copy(status = ReservationStatus.CANCELLED) else it
                }
            )
        }
        announceNewPasses()
    }

    fun loadRoutePath(tripId: String, forceRefresh: Boolean = false) {
        val route = _uiState.value.tripBy(tripId)?.route ?: return
        val current = _uiState.value.routePaths[route.id]
        if (current is RoutePathState.Loading) return
        if (!forceRefresh && current != null) return

        _uiState.update { it.copy(routePaths = it.routePaths + (route.id to RoutePathState.Loading)) }
        viewModelScope.launch {
            val result = routePlanner.pathFor(route)
            _uiState.update { it.copy(routePaths = it.routePaths + (route.id to result)) }
        }
    }

    fun startVehicleDemo(tripId: String) {
        demoVehicleJob?.cancel()
        demoVehicleJob = viewModelScope.launch {
            var elapsedMillis = 0L
            while (isActive) {
                val state = _uiState.value
                val path = state.tripBy(tripId)
                    ?.let(state::routePathFor)
                    ?.let { it as? RoutePathState.Ready }
                    ?.path
                val point = path?.points?.pointAtFraction(
                    (elapsedMillis % DEMO_VEHICLE_LOOP_MILLIS).toDouble() / DEMO_VEHICLE_LOOP_MILLIS
                )
                if (point != null) {
                    val position = VehiclePosition(
                        point = point,
                        source = PositionSource.DEMO,
                        updatedAt = LocalTime.now()
                    )
                    _uiState.update {
                        it.copy(demoVehiclePositions = it.demoVehiclePositions + (tripId to position))
                    }
                    elapsedMillis += DEMO_VEHICLE_TICK_MILLIS
                }
                delay(DEMO_VEHICLE_TICK_MILLIS)
            }
        }
    }

    fun stopVehicleDemo() {
        demoVehicleJob?.cancel()
        demoVehicleJob = null
    }

    fun reset() {
        stopVehicleDemo()
        reservationSequence = 0
        paymentSequence = 0
        _uiState.value = initialState()
        knownPassReservationIds = _uiState.value.availablePassReservationIds
        viewModelScope.launch { imageStore.clear(RECEIPTS_FOLDER) }
    }

    private fun updatePayment(reservationId: String, transform: (Payment) -> Payment) {
        _uiState.update { current ->
            current.copy(
                payments = current.payments.map {
                    if (it.reservationId == reservationId) transform(it) else it
                }
            )
        }
        announceNewPasses()
    }

    private fun announceNewPasses() {
        val state = _uiState.value
        val available = state.availablePassReservationIds
        (available - knownPassReservationIds).forEach { reservationId ->
            val trip = state.reservationBy(reservationId)?.let { state.tripBy(it.tripId) }
            notifier.notify(NotificationMessages.passAvailable(reservationId, trip))
        }
        knownPassReservationIds = available
    }

    private fun initialState() = StudentUiState(
        trips = MockTrips.all,
        reservations = MockStudentData.reservations,
        payments = MockStudentData.payments,
        passes = MockStudentData.passes
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as UniRutaApplication
                StudentViewModel(
                    routePlanner = application.container.routePlanner,
                    imageStore = application.container.imageStore,
                    notifier = application.container.notifier
                )
            }
        }
    }
}
