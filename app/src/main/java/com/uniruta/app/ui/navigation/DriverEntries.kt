package com.uniruta.app.ui.navigation

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.uniruta.app.data.mock.MockDriverData
import com.uniruta.app.data.model.EventOrigin
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.device.qr.QrCodeScanner
import com.uniruta.app.device.qr.QrScanResult
import com.uniruta.app.ui.permissions.openAppSettings
import com.uniruta.app.ui.permissions.openLocationSettings
import com.uniruta.app.ui.permissions.rememberLocationPermissionRequest
import com.uniruta.app.ui.screens.driver.ActiveTripScreen
import com.uniruta.app.ui.screens.driver.BoardingResultScreen
import com.uniruta.app.ui.screens.driver.DriverHomeScreen
import com.uniruta.app.ui.screens.driver.DriverPassengersScreen
import com.uniruta.app.ui.screens.driver.DriverTripDetailScreen
import com.uniruta.app.ui.screens.driver.DriverTripsScreen
import com.uniruta.app.ui.screens.driver.QrScannerScreen
import com.uniruta.app.ui.screens.driver.ReportIncidentScreen
import com.uniruta.app.ui.screens.driver.TripFinishedScreen
import com.uniruta.app.viewmodel.DriverUiState
import com.uniruta.app.viewmodel.DriverViewModel

fun EntryProviderScope<NavKey>.driverEntries(
    uiState: State<DriverUiState>,
    viewModel: DriverViewModel,
    user: SessionUser?,
    onLogout: () -> Unit,
    backStack: NavBackStack<NavKey>
) {
    val state by uiState

    fun backToHome() {
        backStack.clear()
        backStack.add(DriverHome)
    }

    fun openActiveTrip() {
        backStack.clear()
        backStack.add(DriverHome)
        backStack.add(ActiveTrip)
    }

    entry<DriverHome> {
        user?.let { current ->
            DriverHomeScreen(
                user = current,
                hasActiveTrip = state.activeTrip != null,
                onOpenTrips = { backStack.add(DriverTrips) },
                onStartTrip = {
                    val startable = state.startableTrip
                    when {
                        state.activeTrip != null -> openActiveTrip()
                        startable != null -> backStack.add(DriverTripDetail(startable.id))
                        else -> backStack.add(DriverTrips)
                    }
                },
                onValidateBoarding = { backStack.add(QrScannerMock) },
                onLogout = onLogout
            )
        }
    }

    entry<DriverTrips> {
        DriverTripsScreen(
            trips = state.trips,
            driverName = user?.name.orEmpty(),
            onTripSelected = { backStack.add(DriverTripDetail(it)) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<DriverTripDetail> { key ->
        val startWithLocation = rememberLocationPermissionRequest {
            viewModel.startTrip(key.tripId)
            openActiveTrip()
        }
        LaunchedEffect(key.tripId) { viewModel.loadRoutePath(key.tripId) }

        state.tripBy(key.tripId)?.let { trip ->
            DriverTripDetailScreen(
                trip = trip,
                passengerCount = state.passengersFor(trip.id).size,
                startBlockedReason = if (state.activeTripId != null) {
                    "Ya tienes un recorrido en curso. Finalízalo antes de iniciar otro."
                } else {
                    null
                },
                pathState = state.routePathFor(trip),
                onRetryRoute = { viewModel.loadRoutePath(trip.id, forceRefresh = true) },
                onStartTrip = startWithLocation,
                onOpenActiveTrip = ::openActiveTrip,
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }

    entry<ActiveTrip> {
        val context = LocalContext.current
        val requestLocation = rememberLocationPermissionRequest { viewModel.refreshLocationAccess() }

        state.activeTrip?.let { trip ->
            ActiveTripScreen(
                trip = trip,
                pathState = state.routePathFor(trip),
                gpsStatus = state.gpsStatus,
                deviceLocation = state.deviceLocation,
                sensorStatus = state.sensorStatus,
                accelerometer = state.accelerometer,
                gyroscope = state.gyroscope,
                boardedCount = state.boardedCountFor(trip.id),
                passengerCount = state.passengersFor(trip.id).size,
                anomalies = state.anomaliesFor(trip.id),
                incidentCount = state.incidentsFor(trip.id).size,
                onRequestLocation = requestLocation,
                onOpenLocationSettings = { context.openLocationSettings() },
                onOpenAppSettings = { context.openAppSettings() },
                onRetryRoute = { viewModel.loadRoutePath(trip.id, forceRefresh = true) },
                onAddDemoAnomaly = viewModel::addDemoAnomaly,
                onValidateBoarding = { backStack.add(QrScannerMock) },
                onOpenPassengers = { backStack.add(DriverPassengers) },
                onReportIncident = { backStack.add(ReportIncident) },
                onFinishTrip = {
                    val finishedTripId = trip.id
                    viewModel.finishTrip()
                    backStack.clear()
                    backStack.add(DriverHome)
                    backStack.add(TripFinished(finishedTripId))
                },
                onBack = ::backToHome
            )
        }
    }

    entry<QrScannerMock> {
        val context = LocalContext.current
        val scanner = remember(context) { QrCodeScanner(context) }
        var scanError by rememberSaveable { mutableStateOf<String?>(null) }

        fun validate(token: String) {
            scanError = null
            viewModel.validateToken(token)
            backStack.add(BoardingResult)
        }

        QrScannerScreen(
            routeName = state.activeTrip?.route?.name,
            scanError = scanError,
            demoScans = MockDriverData.demoScans,
            onScanWithCamera = {
                scanError = null
                scanner.scan { result ->
                    when (result) {
                        is QrScanResult.Success -> validate(result.content)
                        QrScanResult.Cancelled -> Unit
                        is QrScanResult.Failure -> scanError = result.message
                    }
                }
            },
            onDemoScan = { scan -> validate(scan.token) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<BoardingResult> {
        state.lastValidation?.let { validation ->
            val passenger = state.passengerBy(validation.reservationId)
            BoardingResultScreen(
                outcome = validation.outcome,
                passenger = passenger,
                routeName = passenger
                    ?.let { state.tripBy(it.tripId)?.route?.name }
                    .orEmpty(),
                onRegisterBoarding = {
                    validation.reservationId?.let(viewModel::registerBoarding)
                },
                onScanAgain = {
                    viewModel.clearValidation()
                    backStack.removeLastOrNull()
                },
                onBackToTrip = {
                    viewModel.clearValidation()
                    openActiveTrip()
                }
            )
        }
    }

    entry<DriverPassengers> {
        DriverPassengersScreen(
            passengers = state.activeTripId
                ?.let(state::passengersFor)
                .orEmpty(),
            onConfirmCashPayment = viewModel::confirmCashPayment,
            onRegisterBoarding = viewModel::registerBoarding,
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<ReportIncident> {
        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            viewModel.onEvidenceCaptured(saved)
        }

        ReportIncidentScreen(
            evidence = state.evidence,
            evidenceBusy = state.evidenceBusy,
            evidenceError = state.evidenceError,
            onTakePhoto = {
                viewModel.prepareEvidenceCapture()?.let { target ->
                    try {
                        camera.launch(Uri.parse(target))
                    } catch (error: ActivityNotFoundException) {
                        viewModel.onEvidenceCaptureUnavailable()
                    }
                }
            },
            onRemovePhoto = viewModel::removeEvidence,
            onSubmit = viewModel::reportIncident,
            onBack = {
                viewModel.discardIncidentDraft()
                backStack.removeLastOrNull()
            }
        )
    }

    entry<TripFinished> { key ->
        state.tripBy(key.tripId)?.let { trip ->
            TripFinishedScreen(
                trip = trip,
                boardedCount = state.boardedCountFor(trip.id),
                passengerCount = state.passengersFor(trip.id).size,
                sensorAnomalyCount = state.anomalyCountFor(trip.id, EventOrigin.SENSOR),
                demoAnomalyCount = state.anomalyCountFor(trip.id, EventOrigin.DEMO),
                incidentCount = state.incidentsFor(trip.id).size,
                onBackHome = ::backToHome
            )
        }
    }
}
