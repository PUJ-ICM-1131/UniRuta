package com.uniruta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uniruta.app.UniRutaApplication
import com.uniruta.app.data.mock.MockDriverData
import com.uniruta.app.data.model.AnomalyEvent
import com.uniruta.app.data.model.AnomalyType
import com.uniruta.app.data.model.EventOrigin
import com.uniruta.app.data.model.Incident
import com.uniruta.app.data.model.IncidentType
import com.uniruta.app.data.model.PassStatus
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.PositionSource
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.SensorSource
import com.uniruta.app.data.model.TrackingStatus
import com.uniruta.app.data.model.Trip
import com.uniruta.app.data.model.TripPassenger
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.model.VehiclePosition
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.data.routing.RoutePlanner
import com.uniruta.app.data.storage.CaptureTarget
import com.uniruta.app.data.storage.EVIDENCE_FOLDER
import com.uniruta.app.data.storage.ImageResult
import com.uniruta.app.data.storage.ImageStore
import com.uniruta.app.data.storage.StoredImage
import com.uniruta.app.device.location.LocationEvent
import com.uniruta.app.device.location.LocationReadiness
import com.uniruta.app.device.location.LocationTracker
import com.uniruta.app.device.sensors.MotionAnomalyDetector
import com.uniruta.app.device.sensors.MotionSample
import com.uniruta.app.device.sensors.MotionSensorSource
import com.uniruta.app.notifications.AppNotifier
import com.uniruta.app.notifications.NotificationMessages
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

private const val READING_PUBLISH_INTERVAL_NANOS = 250_000_000L
private const val EVIDENCE_BASE_NAME = "evidencia"

enum class ValidationOutcome {
    VALID,
    NOT_FOUND,
    WRONG_TRIP,
    NOT_CONFIRMED,
    ALREADY_USED
}

data class ValidationResult(
    val outcome: ValidationOutcome,
    val reservationId: String? = null
)

data class SensorReading(
    val x: Float,
    val y: Float,
    val z: Float
)

data class MotionSensorState(
    val status: TrackingStatus = TrackingStatus.INACTIVE,
    val reading: SensorReading? = null
)

data class DriverUiState(
    val trips: List<Trip> = emptyList(),
    val passengers: List<TripPassenger> = emptyList(),
    val activeTripId: String? = null,
    val gpsStatus: TrackingStatus = TrackingStatus.INACTIVE,
    val deviceLocation: VehiclePosition? = null,
    val accelerometer: MotionSensorState = MotionSensorState(),
    val gyroscope: MotionSensorState = MotionSensorState(),
    val anomalies: List<AnomalyEvent> = emptyList(),
    val incidents: List<Incident> = emptyList(),
    val lastValidation: ValidationResult? = null,
    val routePaths: Map<String, RoutePathState> = emptyMap(),
    val evidence: StoredImage? = null,
    val evidenceBusy: Boolean = false,
    val evidenceError: String? = null
) {
    val activeTrip: Trip?
        get() = activeTripId?.let(::tripBy)

    val startableTrip: Trip?
        get() = trips.firstOrNull { it.status == TripStatus.SCHEDULED }

    val sensorStatus: TrackingStatus
        get() {
            val statuses = listOf(accelerometer.status, gyroscope.status)
            return when {
                TrackingStatus.ACTIVE in statuses -> TrackingStatus.ACTIVE
                TrackingStatus.SEARCHING in statuses -> TrackingStatus.SEARCHING
                TrackingStatus.PAUSED in statuses -> TrackingStatus.PAUSED
                TrackingStatus.ERROR in statuses -> TrackingStatus.ERROR
                statuses.all { it == TrackingStatus.UNAVAILABLE } -> TrackingStatus.UNAVAILABLE
                else -> TrackingStatus.INACTIVE
            }
        }

    val hasScenarioChanges: Boolean
        get() = activeTripId != null ||
            anomalies.isNotEmpty() ||
            incidents.isNotEmpty() ||
            trips != MockDriverData.assignedTrips ||
            passengers != MockDriverData.passengers

    fun tripBy(id: String): Trip? = trips.firstOrNull { it.id == id }

    fun routePathFor(trip: Trip): RoutePathState? = routePaths[trip.route.id]

    fun passengersFor(tripId: String): List<TripPassenger> =
        passengers.filter { it.tripId == tripId && it.reservation.isActive }

    fun passengerBy(reservationId: String?): TripPassenger? =
        passengers.firstOrNull { it.reservation.id == reservationId }

    fun anomaliesFor(tripId: String): List<AnomalyEvent> =
        anomalies.filter { it.tripId == tripId }

    fun anomalyCountFor(tripId: String, origin: EventOrigin): Int =
        anomaliesFor(tripId).count { it.origin == origin }

    fun incidentsFor(tripId: String): List<Incident> =
        incidents.filter { it.tripId == tripId }

    fun boardedCountFor(tripId: String): Int =
        passengersFor(tripId).count { it.hasBoarded }
}

class DriverViewModel(
    private val locationTracker: LocationTracker,
    private val motionSensors: MotionSensorSource,
    private val routePlanner: RoutePlanner,
    private val imageStore: ImageStore,
    private val detector: MotionAnomalyDetector = MotionAnomalyDetector(),
    private val notifier: AppNotifier = AppNotifier.None
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private var anomalySequence = 0
    private var incidentSequence = 0
    private var demoAnomalySequence = 0
    private var appVisible = true
    private var monitoringAllowed = true
    private var locationJob: Job? = null
    private var sensorJob: Job? = null
    private var pendingCapture: CaptureTarget? = null
    private val lastReadingNanos = mutableMapOf<SensorSource, Long>()

    init {
        viewModelScope.launch { imageStore.clear(EVIDENCE_FOLDER) }
    }

    fun startTrip(tripId: String) {
        val state = _uiState.value
        if (state.activeTripId != null) return
        val trip = state.tripBy(tripId) ?: return
        if (trip.status != TripStatus.SCHEDULED) return

        _uiState.update { current ->
            current.copy(
                trips = current.trips.map {
                    if (it.id == tripId) it.copy(status = TripStatus.IN_PROGRESS) else it
                },
                activeTripId = tripId
            )
        }
        loadRoutePath(tripId)
        refreshMonitoring()
        notifier.notify(NotificationMessages.tripStarted(trip))
    }

    fun finishTrip() {
        val state = _uiState.value
        val tripId = state.activeTripId ?: return
        val trip = state.tripBy(tripId)
        _uiState.update { current ->
            current.copy(
                trips = current.trips.map {
                    if (it.id == tripId) it.copy(status = TripStatus.COMPLETED) else it
                },
                activeTripId = null,
                lastValidation = null
            )
        }
        refreshMonitoring()
        if (trip != null) {
            notifier.notify(
                NotificationMessages.tripFinished(
                    trip = trip,
                    boardedCount = state.boardedCountFor(tripId),
                    passengerCount = state.passengersFor(tripId).size
                )
            )
        }
    }

    fun onAppVisibilityChanged(visible: Boolean) {
        if (appVisible == visible) return
        appVisible = visible
        refreshMonitoring()
    }

    fun onMonitoringAllowedChanged(allowed: Boolean) {
        if (monitoringAllowed == allowed) return
        monitoringAllowed = allowed
        refreshMonitoring()
    }

    fun refreshLocationAccess() {
        if (locationJob?.isActive != true) refreshMonitoring()
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

    fun addDemoAnomaly() {
        val tripId = _uiState.value.activeTripId ?: return
        val type = AnomalyType.entries[demoAnomalySequence++ % AnomalyType.entries.size]
        val event = newAnomaly(tripId, type, EventOrigin.DEMO, magnitude = null)
        _uiState.update { it.copy(anomalies = it.anomalies + event) }
    }

    fun validateToken(token: String) {
        val state = _uiState.value
        val passenger = state.passengers.firstOrNull { it.pass?.token == token }
        val pass = passenger?.pass

        val result = when {
            passenger == null || pass == null -> ValidationResult(ValidationOutcome.NOT_FOUND)
            passenger.tripId != state.activeTripId ->
                ValidationResult(ValidationOutcome.WRONG_TRIP, passenger.reservation.id)
            pass.status != PassStatus.ACTIVE ->
                ValidationResult(ValidationOutcome.ALREADY_USED, passenger.reservation.id)
            passenger.reservation.status != ReservationStatus.CONFIRMED ->
                ValidationResult(ValidationOutcome.NOT_CONFIRMED, passenger.reservation.id)
            else -> ValidationResult(ValidationOutcome.VALID, passenger.reservation.id)
        }

        _uiState.update { it.copy(lastValidation = result) }
    }

    fun clearValidation() {
        _uiState.update { it.copy(lastValidation = null) }
    }

    fun registerBoarding(reservationId: String) {
        val passenger = _uiState.value.passengerBy(reservationId) ?: return
        if (!passenger.canRegisterBoarding) return

        _uiState.update { current ->
            current.copy(
                passengers = current.passengers.map { item ->
                    if (item.reservation.id != reservationId) {
                        item
                    } else {
                        item.copy(
                            reservation = item.reservation.copy(status = ReservationStatus.BOARDED),
                            pass = item.pass?.copy(status = PassStatus.USED)
                        )
                    }
                }
            )
        }
    }

    fun confirmCashPayment(reservationId: String) {
        _uiState.update { current ->
            current.copy(
                passengers = current.passengers.map { item ->
                    if (item.reservation.id != reservationId || !item.awaitsCashOnBoarding) {
                        item
                    } else {
                        item.copy(payment = item.payment.copy(status = PaymentStatus.CONFIRMED))
                    }
                }
            )
        }
    }

    fun prepareEvidenceCapture(): String? {
        discardPendingCapture()
        val target = imageStore.createCaptureTarget(EVIDENCE_FOLDER, EVIDENCE_BASE_NAME)
        if (target == null) {
            _uiState.update {
                it.copy(evidenceError = "No fue posible preparar el archivo para la fotografía.")
            }
            return null
        }
        pendingCapture = target
        _uiState.update { it.copy(evidenceError = null) }
        return target.contentUri
    }

    fun onEvidenceCaptured(reportedSuccess: Boolean) {
        val target = pendingCapture ?: return
        pendingCapture = null
        _uiState.update { it.copy(evidenceBusy = true) }
        viewModelScope.launch {
            when (val result = imageStore.confirmCapture(target)) {
                is ImageResult.Success -> {
                    _uiState.value.evidence?.let { imageStore.delete(it.path) }
                    _uiState.update {
                        it.copy(evidence = result.image, evidenceBusy = false, evidenceError = null)
                    }
                }
                is ImageResult.Failure -> _uiState.update {
                    it.copy(
                        evidenceBusy = false,
                        evidenceError = if (reportedSuccess) result.message else null
                    )
                }
            }
        }
    }

    fun onEvidenceCaptureUnavailable() {
        discardPendingCapture()
        _uiState.update {
            it.copy(evidenceError = "No hay una aplicación de cámara disponible en este dispositivo.")
        }
    }

    fun removeEvidence() {
        val evidence = _uiState.value.evidence ?: return
        _uiState.update { it.copy(evidence = null, evidenceError = null) }
        viewModelScope.launch { imageStore.delete(evidence.path) }
    }

    fun discardIncidentDraft() {
        discardPendingCapture()
        removeEvidence()
        _uiState.update { it.copy(evidenceError = null) }
    }

    fun reportIncident(type: IncidentType, description: String) {
        val state = _uiState.value
        val tripId = state.activeTripId ?: return
        val evidence = state.evidence
        val incident = Incident(
            id = "inc-${++incidentSequence + 5000}",
            tripId = tripId,
            type = type,
            description = description.trim(),
            evidenceName = evidence?.displayName,
            reportedAt = LocalTime.now(),
            evidencePath = evidence?.path
        )
        _uiState.update {
            it.copy(incidents = it.incidents + incident, evidence = null, evidenceError = null)
        }
        notifier.notify(NotificationMessages.incidentReported(incident, state.tripBy(tripId)))
    }

    fun reset() {
        locationJob?.cancel()
        sensorJob?.cancel()
        locationJob = null
        sensorJob = null
        pendingCapture = null
        anomalySequence = 0
        incidentSequence = 0
        demoAnomalySequence = 0
        lastReadingNanos.clear()
        detector.reset()
        _uiState.value = initialState()
        viewModelScope.launch { imageStore.clear(EVIDENCE_FOLDER) }
    }

    private fun refreshMonitoring() {
        when {
            _uiState.value.activeTripId == null -> {
                stopLocation(TrackingStatus.INACTIVE)
                stopSensors(TrackingStatus.INACTIVE)
            }
            !appVisible || !monitoringAllowed -> {
                stopLocation(TrackingStatus.PAUSED)
                stopSensors(TrackingStatus.PAUSED)
            }
            else -> {
                startLocation()
                startSensors()
            }
        }
    }

    private fun startLocation() {
        val readiness = locationTracker.readiness()
        when (readiness) {
            LocationReadiness.PERMISSION_DENIED -> {
                stopLocation(TrackingStatus.PERMISSION_DENIED)
                return
            }
            LocationReadiness.SERVICES_UNAVAILABLE -> {
                stopLocation(TrackingStatus.UNAVAILABLE)
                return
            }
            LocationReadiness.PROVIDER_DISABLED, LocationReadiness.READY -> Unit
        }
        if (locationJob?.isActive == true) return

        setGpsStatus(
            if (readiness == LocationReadiness.PROVIDER_DISABLED) TrackingStatus.DISABLED else TrackingStatus.SEARCHING
        )
        locationJob = viewModelScope.launch {
            locationTracker.updates()
                .catch { setGpsStatus(statusAfterLocationFailure()) }
                .collect(::onLocationEvent)
        }
    }

    private fun stopLocation(status: TrackingStatus) {
        locationJob?.cancel()
        locationJob = null
        _uiState.update {
            it.copy(
                gpsStatus = status,
                deviceLocation = if (status == TrackingStatus.PAUSED) it.deviceLocation else null
            )
        }
    }

    private fun onLocationEvent(event: LocationEvent) {
        when (event) {
            is LocationEvent.Fix -> _uiState.update {
                it.copy(
                    gpsStatus = TrackingStatus.ACTIVE,
                    deviceLocation = VehiclePosition(
                        point = event.point,
                        source = PositionSource.DEVICE_GPS,
                        updatedAt = LocalTime.now(),
                        accuracyMeters = event.accuracyMeters
                    )
                )
            }
            LocationEvent.SignalLost -> setGpsStatus(
                if (locationTracker.readiness() == LocationReadiness.PROVIDER_DISABLED) {
                    TrackingStatus.DISABLED
                } else {
                    TrackingStatus.SEARCHING
                }
            )
        }
    }

    private fun statusAfterLocationFailure(): TrackingStatus = when (locationTracker.readiness()) {
        LocationReadiness.PERMISSION_DENIED -> TrackingStatus.PERMISSION_DENIED
        LocationReadiness.SERVICES_UNAVAILABLE -> TrackingStatus.UNAVAILABLE
        LocationReadiness.PROVIDER_DISABLED -> TrackingStatus.DISABLED
        LocationReadiness.READY -> TrackingStatus.ERROR
    }

    private fun setGpsStatus(status: TrackingStatus) {
        _uiState.update { it.copy(gpsStatus = status) }
    }

    private fun startSensors() {
        if (sensorJob?.isActive == true) return
        val accelerometerAvailable = motionSensors.isAvailable(SensorSource.ACCELEROMETER)
        val gyroscopeAvailable = motionSensors.isAvailable(SensorSource.GYROSCOPE)
        _uiState.update {
            it.copy(
                accelerometer = it.accelerometer.copy(status = initialSensorStatus(accelerometerAvailable)),
                gyroscope = it.gyroscope.copy(status = initialSensorStatus(gyroscopeAvailable))
            )
        }
        if (!accelerometerAvailable && !gyroscopeAvailable) return

        detector.reset()
        lastReadingNanos.clear()
        sensorJob = viewModelScope.launch {
            motionSensors.samples()
                .catch { markSensorFailure() }
                .collect(::onMotionSample)
        }
    }

    private fun stopSensors(status: TrackingStatus) {
        sensorJob?.cancel()
        sensorJob = null
        _uiState.update {
            it.copy(
                accelerometer = it.accelerometer.stoppedAs(status),
                gyroscope = it.gyroscope.stoppedAs(status)
            )
        }
    }

    private fun onMotionSample(sample: MotionSample) {
        val tripId = _uiState.value.activeTripId ?: return
        val detection = detector.process(sample)
        val last = lastReadingNanos[sample.source]
        val publishReading = last == null || sample.timestampNanos - last >= READING_PUBLISH_INTERVAL_NANOS
        if (publishReading) lastReadingNanos[sample.source] = sample.timestampNanos
        if (!publishReading && detection == null) return

        val event = detection?.let {
            newAnomaly(tripId, it.type, EventOrigin.SENSOR, magnitude = it.magnitude)
        }
        _uiState.update { current ->
            val withReading = if (publishReading) current.withReading(sample) else current
            if (event == null) withReading else withReading.copy(anomalies = withReading.anomalies + event)
        }
    }

    private fun markSensorFailure() {
        _uiState.update {
            it.copy(
                accelerometer = it.accelerometer.failed(),
                gyroscope = it.gyroscope.failed()
            )
        }
    }

    private fun newAnomaly(
        tripId: String,
        type: AnomalyType,
        origin: EventOrigin,
        magnitude: Float?
    ): AnomalyEvent = AnomalyEvent(
        id = "anm-${++anomalySequence + 7000}",
        tripId = tripId,
        type = type,
        time = LocalTime.now(),
        origin = origin,
        magnitude = magnitude
    )

    private fun discardPendingCapture() {
        val target = pendingCapture ?: return
        pendingCapture = null
        viewModelScope.launch { imageStore.delete(target.path) }
    }

    private fun initialSensorStatus(available: Boolean): TrackingStatus =
        if (available) TrackingStatus.SEARCHING else TrackingStatus.UNAVAILABLE

    private fun MotionSensorState.stoppedAs(status: TrackingStatus): MotionSensorState = when {
        status == TrackingStatus.INACTIVE -> MotionSensorState()
        this.status == TrackingStatus.UNAVAILABLE -> this
        else -> copy(status = status)
    }

    private fun MotionSensorState.failed(): MotionSensorState =
        if (status == TrackingStatus.UNAVAILABLE) this else copy(status = TrackingStatus.ERROR)

    private fun DriverUiState.withReading(sample: MotionSample): DriverUiState = when (sample.source) {
        SensorSource.ACCELEROMETER -> copy(
            accelerometer = MotionSensorState(
                status = TrackingStatus.ACTIVE,
                reading = SensorReading(sample.x, sample.y, sample.z)
            )
        )
        SensorSource.GYROSCOPE -> copy(
            gyroscope = MotionSensorState(
                status = TrackingStatus.ACTIVE,
                reading = SensorReading(
                    x = Math.toDegrees(sample.x.toDouble()).toFloat(),
                    y = Math.toDegrees(sample.y.toDouble()).toFloat(),
                    z = Math.toDegrees(sample.z.toDouble()).toFloat()
                )
            )
        )
    }

    private fun initialState() = DriverUiState(
        trips = MockDriverData.assignedTrips,
        passengers = MockDriverData.passengers
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as UniRutaApplication
                val container = application.container
                DriverViewModel(
                    locationTracker = container.locationTracker,
                    motionSensors = container.motionSensors,
                    routePlanner = container.routePlanner,
                    imageStore = container.imageStore,
                    notifier = container.notifier
                )
            }
        }
    }
}
