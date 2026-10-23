package com.uniruta.app.viewmodel

import com.uniruta.app.data.mock.MockDriverData
import com.uniruta.app.data.model.AnomalyType
import com.uniruta.app.data.model.EventOrigin
import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.IncidentType
import com.uniruta.app.data.model.PositionSource
import com.uniruta.app.data.model.SensorSource
import com.uniruta.app.data.model.TrackingStatus
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.routing.DirectionsError
import com.uniruta.app.data.routing.DirectionsResult
import com.uniruta.app.data.routing.RoutePathState
import com.uniruta.app.data.routing.RoutePlanner
import com.uniruta.app.data.storage.CaptureTarget
import com.uniruta.app.data.storage.ImageResult
import com.uniruta.app.data.storage.ImageStore
import com.uniruta.app.data.storage.StoredImage
import com.uniruta.app.device.location.LocationEvent
import com.uniruta.app.device.location.LocationReadiness
import com.uniruta.app.device.location.LocationTracker
import com.uniruta.app.device.sensors.MotionSample
import com.uniruta.app.device.sensors.MotionSensorSource
import com.uniruta.app.device.sensors.STANDARD_GRAVITY
import com.uniruta.app.notifications.AppNotification
import com.uniruta.app.notifications.AppNotifier
import com.uniruta.app.notifications.NotificationDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val ACTIVE_TRIP = "trip-001"

@OptIn(ExperimentalCoroutinesApi::class)
class DriverViewModelTest {

    private class FakeLocationTracker(var readiness: LocationReadiness = LocationReadiness.READY) : LocationTracker {
        val events = MutableSharedFlow<LocationEvent>(extraBufferCapacity = 16)
        override fun readiness(): LocationReadiness = readiness
        override fun updates(): Flow<LocationEvent> = events
    }

    private class FakeMotionSensors(private val available: Set<SensorSource>) : MotionSensorSource {
        val samples = MutableSharedFlow<MotionSample>(extraBufferCapacity = 512)
        override fun isAvailable(source: SensorSource): Boolean = source in available
        override fun samples(): Flow<MotionSample> = samples
    }

    private class FakeImageStore : ImageStore {
        val deleted = mutableListOf<String>()
        var capturedBytes = 2048L
        override suspend fun importImage(sourceUri: String, folder: String, baseName: String): ImageResult =
            ImageResult.Success(StoredImage("comprobante.jpg", "/files/$folder/$baseName.jpg", 1024))
        override fun createCaptureTarget(folder: String, baseName: String): CaptureTarget =
            CaptureTarget(path = "/files/$folder/$baseName.jpg", contentUri = "content://test/$baseName.jpg")
        override suspend fun confirmCapture(target: CaptureTarget): ImageResult =
            if (capturedBytes > 0) {
                ImageResult.Success(StoredImage("evidencia.jpg", target.path, capturedBytes))
            } else {
                ImageResult.Failure("No se recibió ninguna fotografía de la cámara.")
            }
        override suspend fun delete(path: String) {
            deleted += path
        }
        override suspend fun clear(folder: String) = Unit
    }

    private class RecordingNotifier : AppNotifier {
        val sent = mutableListOf<AppNotification>()
        override fun notify(notification: AppNotification) {
            sent += notification
        }
    }

    private val dispatcher = UnconfinedTestDispatcher()
    private val notifier = RecordingNotifier()
    private lateinit var location: FakeLocationTracker
    private lateinit var sensors: FakeMotionSensors
    private lateinit var images: FakeImageStore
    private lateinit var viewModel: DriverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        createViewModel(available = SensorSource.entries.toSet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        available: Set<SensorSource>,
        readiness: LocationReadiness = LocationReadiness.READY
    ) {
        location = FakeLocationTracker(readiness)
        sensors = FakeMotionSensors(available)
        images = FakeImageStore()
        viewModel = DriverViewModel(
            locationTracker = location,
            motionSensors = sensors,
            routePlanner = RoutePlanner { DirectionsResult.Failure(DirectionsError.MISSING_KEY) },
            imageStore = images,
            notifier = notifier
        )
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun startingTripDoesNotInventAnomaliesAndStartsRealMonitoring() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)

        assertEquals(TripStatus.IN_PROGRESS, state.tripBy(ACTIVE_TRIP)?.status)
        assertTrue(state.anomalies.isEmpty())
        assertEquals(TrackingStatus.SEARCHING, state.gpsStatus)
        assertEquals(1, location.events.subscriptionCount.value)
        assertEquals(1, sensors.samples.subscriptionCount.value)
        assertTrue(state.routePathFor(state.activeTrip!!) is RoutePathState.Ready)
    }

    @Test
    fun locationFixUpdatesPositionAndStatus() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)

        location.events.emit(LocationEvent.Fix(GeoPoint(4.75, -74.04), accuracyMeters = 8f))

        assertEquals(TrackingStatus.ACTIVE, state.gpsStatus)
        assertEquals(GeoPoint(4.75, -74.04), state.deviceLocation?.point)
        assertEquals(PositionSource.DEVICE_GPS, state.deviceLocation?.source)
    }

    @Test
    fun deniedPermissionNeverReportsSharingAndCanBeGrantedLater() = runTest {
        createViewModel(available = SensorSource.entries.toSet(), readiness = LocationReadiness.PERMISSION_DENIED)

        viewModel.startTrip(ACTIVE_TRIP)

        assertEquals(TrackingStatus.PERMISSION_DENIED, state.gpsStatus)
        assertEquals(0, location.events.subscriptionCount.value)

        location.readiness = LocationReadiness.READY
        viewModel.refreshLocationAccess()
        location.events.emit(LocationEvent.Fix(GeoPoint(4.7, -74.05), accuracyMeters = null))

        assertEquals(TrackingStatus.ACTIVE, state.gpsStatus)
        assertEquals(1, location.events.subscriptionCount.value)
    }

    @Test
    fun disabledProviderIsReported() = runTest {
        createViewModel(available = SensorSource.entries.toSet(), readiness = LocationReadiness.PROVIDER_DISABLED)

        viewModel.startTrip(ACTIVE_TRIP)
        location.events.emit(LocationEvent.SignalLost)

        assertEquals(TrackingStatus.DISABLED, state.gpsStatus)
    }

    @Test
    fun backgroundPausesAndForegroundResumesWithoutDuplicates() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)

        viewModel.onAppVisibilityChanged(false)

        assertEquals(TrackingStatus.PAUSED, state.gpsStatus)
        assertEquals(TrackingStatus.PAUSED, state.accelerometer.status)
        assertEquals(0, location.events.subscriptionCount.value)
        assertEquals(0, sensors.samples.subscriptionCount.value)

        viewModel.onAppVisibilityChanged(true)
        viewModel.onAppVisibilityChanged(true)
        viewModel.refreshLocationAccess()

        assertEquals(1, location.events.subscriptionCount.value)
        assertEquals(1, sensors.samples.subscriptionCount.value)
    }

    @Test
    fun finishingTripReleasesListeners() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        location.events.emit(LocationEvent.Fix(GeoPoint(4.75, -74.04), accuracyMeters = 5f))

        viewModel.finishTrip()

        assertEquals(TripStatus.COMPLETED, state.tripBy(ACTIVE_TRIP)?.status)
        assertEquals(TrackingStatus.INACTIVE, state.gpsStatus)
        assertNull(state.deviceLocation)
        assertEquals(TrackingStatus.INACTIVE, state.sensorStatus)
        assertEquals(0, location.events.subscriptionCount.value)
        assertEquals(0, sensors.samples.subscriptionCount.value)
    }

    @Test
    fun missingGyroscopeIsReportedAsUnavailable() = runTest {
        createViewModel(available = setOf(SensorSource.ACCELEROMETER))

        viewModel.startTrip(ACTIVE_TRIP)
        sensors.samples.emit(MotionSample(SensorSource.ACCELEROMETER, 0f, STANDARD_GRAVITY, 0f, 0L))

        assertEquals(TrackingStatus.ACTIVE, state.accelerometer.status)
        assertEquals(TrackingStatus.UNAVAILABLE, state.gyroscope.status)
        assertEquals(TrackingStatus.ACTIVE, state.sensorStatus)
    }

    @Test
    fun sensorDetectionsAreRecordedAsRealReadings() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        var nanos = 0L
        repeat(150) {
            sensors.samples.emit(MotionSample(SensorSource.ACCELEROMETER, 0f, STANDARD_GRAVITY, 0f, nanos))
            nanos += 20_000_000L
        }
        repeat(50) {
            sensors.samples.emit(MotionSample(SensorSource.ACCELEROMETER, 0f, STANDARD_GRAVITY, 4.5f, nanos))
            nanos += 20_000_000L
        }

        val event = state.anomaliesFor(ACTIVE_TRIP).single()
        assertEquals(AnomalyType.HARD_BRAKING, event.type)
        assertEquals(EventOrigin.SENSOR, event.origin)
        assertNotNull(event.magnitude)
        assertNotNull(state.accelerometer.reading)
    }

    @Test
    fun demoAnomaliesAreLabelledAndCountedSeparately() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)

        viewModel.addDemoAnomaly()

        val event = state.anomaliesFor(ACTIVE_TRIP).single()
        assertEquals(EventOrigin.DEMO, event.origin)
        assertNull(event.magnitude)
        assertEquals(1, state.anomalyCountFor(ACTIVE_TRIP, EventOrigin.DEMO))
        assertEquals(0, state.anomalyCountFor(ACTIVE_TRIP, EventOrigin.SENSOR))
    }

    @Test
    fun scannedTokensKeepExistingValidationRules() {
        viewModel.startTrip(ACTIVE_TRIP)
        val outcomes = MockDriverData.demoScans.map { scan ->
            viewModel.validateToken(scan.token)
            state.lastValidation?.outcome
        }

        assertEquals(
            listOf(
                ValidationOutcome.VALID,
                ValidationOutcome.ALREADY_USED,
                ValidationOutcome.WRONG_TRIP,
                ValidationOutcome.NOT_FOUND
            ),
            outcomes
        )
    }

    @Test
    fun samePassCannotBoardTwice() {
        viewModel.startTrip(ACTIVE_TRIP)
        val token = MockDriverData.demoScans.first().token
        val boardedBefore = state.boardedCountFor(ACTIVE_TRIP)

        viewModel.validateToken(token)
        val reservationId = state.lastValidation!!.reservationId!!
        viewModel.registerBoarding(reservationId)
        viewModel.validateToken(token)

        assertEquals(ValidationOutcome.ALREADY_USED, state.lastValidation?.outcome)
        viewModel.registerBoarding(reservationId)
        assertEquals(boardedBefore + 1, state.boardedCountFor(ACTIVE_TRIP))
    }

    @Test
    fun capturedEvidenceIsAttachedToTheIncident() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)

        assertNotNull(viewModel.prepareEvidenceCapture())
        viewModel.onEvidenceCaptured(reportedSuccess = true)
        viewModel.reportIncident(IncidentType.ROAD, "Hueco en la vía")

        val incident = state.incidentsFor(ACTIVE_TRIP).single()
        assertEquals("evidencia.jpg", incident.evidenceName)
        assertNotNull(incident.evidencePath)
        assertNull(state.evidence)
    }

    @Test
    fun cancelledCaptureLeavesTheFormUsable() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        images.capturedBytes = 0

        viewModel.prepareEvidenceCapture()
        viewModel.onEvidenceCaptured(reportedSuccess = false)

        assertNull(state.evidence)
        assertNull(state.evidenceError)
        assertTrue(!state.evidenceBusy)
    }

    @Test
    fun tripLifecycleNotifiesStartIncidentAndFinishOnce() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        viewModel.startTrip(ACTIVE_TRIP)
        viewModel.reportIncident(IncidentType.TRAFFIC, "Congestión en la Autopista Norte")
        viewModel.finishTrip()
        viewModel.finishTrip()

        assertEquals(
            listOf("Recorrido iniciado", "Novedad registrada", "Recorrido finalizado"),
            notifier.sent.map { it.title }
        )
        assertEquals(
            listOf(NotificationDestination.ACTIVE_TRIP, NotificationDestination.ACTIVE_TRIP, NotificationDestination.TRIP_FINISHED),
            notifier.sent.map { it.target.destination }
        )
    }

    @Test
    fun loggingOutPausesHardwareAndReturningResumesIt() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        assertEquals(1, location.events.subscriptionCount.value)

        viewModel.onMonitoringAllowedChanged(false)
        assertEquals(0, location.events.subscriptionCount.value)
        assertEquals(0, sensors.samples.subscriptionCount.value)
        assertEquals(TrackingStatus.PAUSED, state.gpsStatus)

        viewModel.onMonitoringAllowedChanged(true)
        assertEquals(1, location.events.subscriptionCount.value)
        assertEquals(1, sensors.samples.subscriptionCount.value)
    }

    @Test
    fun resetStopsTheTripAndRestoresTheScenario() = runTest {
        viewModel.startTrip(ACTIVE_TRIP)
        viewModel.addDemoAnomaly()
        assertTrue(state.hasScenarioChanges)

        viewModel.reset()

        assertFalse(state.hasScenarioChanges)
        assertEquals(0, location.events.subscriptionCount.value)
        assertEquals(TrackingStatus.INACTIVE, state.gpsStatus)
    }
}
