package com.uniruta.app.ui.navigation

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.device.contacts.PickContactPhone
import com.uniruta.app.ui.permissions.openDialer
import com.uniruta.app.ui.screens.student.ConfirmReservationScreen
import com.uniruta.app.ui.screens.student.PaymentMethodScreen
import com.uniruta.app.ui.screens.student.PaymentOutcomeScreen
import com.uniruta.app.ui.screens.student.ReservationDetailScreen
import com.uniruta.app.ui.screens.student.ReservationRow
import com.uniruta.app.ui.screens.student.StudentHomeScreen
import com.uniruta.app.ui.screens.student.StudentPassScreen
import com.uniruta.app.ui.screens.student.StudentReservationsScreen
import com.uniruta.app.ui.screens.student.StudentTripsScreen
import com.uniruta.app.ui.screens.student.TransferPaymentScreen
import com.uniruta.app.ui.screens.student.TripDetailScreen
import com.uniruta.app.viewmodel.EmergencyContactUiState
import com.uniruta.app.viewmodel.EmergencyContactViewModel
import com.uniruta.app.viewmodel.StudentUiState
import com.uniruta.app.viewmodel.StudentViewModel

fun EntryProviderScope<NavKey>.studentEntries(
    uiState: State<StudentUiState>,
    contactState: State<EmergencyContactUiState>,
    viewModel: StudentViewModel,
    contactViewModel: EmergencyContactViewModel,
    user: SessionUser?,
    onLogout: () -> Unit,
    backStack: NavBackStack<NavKey>
) {
    val state by uiState
    val contact by contactState

    fun backToHome() {
        backStack.clear()
        backStack.add(StudentHome)
    }

    entry<StudentHome> {
        val context = LocalContext.current
        val contactPicker = rememberLauncherForActivityResult(PickContactPhone()) { uri ->
            if (uri != null) contactViewModel.onContactPicked(uri.toString())
        }
        user?.let { current ->
            StudentHomeScreen(
                user = current,
                contactState = contact,
                onOpenTrips = { backStack.add(StudentTrips) },
                onOpenReservations = { backStack.add(StudentReservations) },
                onOpenPass = { backStack.add(StudentPass()) },
                onPickContact = {
                    contactViewModel.clearError()
                    try {
                        contactPicker.launch(Unit)
                    } catch (error: ActivityNotFoundException) {
                        contactViewModel.onPickerUnavailable()
                    }
                },
                onCallContact = { phone ->
                    if (!context.openDialer(phone)) contactViewModel.onDialerUnavailable()
                },
                onRemoveContact = contactViewModel::removeContact,
                onDismissContactError = contactViewModel::clearError,
                onLogout = onLogout
            )
        }
    }

    entry<StudentTrips> {
        StudentTripsScreen(
            trips = state.trips,
            onTripSelected = { backStack.add(TripDetail(it)) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<TripDetail> { key ->
        LaunchedEffect(key.tripId) { viewModel.loadRoutePath(key.tripId) }
        DisposableEffect(key.tripId) {
            viewModel.startVehicleDemo(key.tripId)
            onDispose { viewModel.stopVehicleDemo() }
        }

        state.tripBy(key.tripId)?.let { trip ->
            TripDetailScreen(
                trip = trip,
                alreadyReserved = state.hasActiveReservationFor(trip.id),
                pathState = state.routePathFor(trip),
                demoVehicle = state.demoVehiclePositions[trip.id],
                onRetryRoute = { viewModel.loadRoutePath(trip.id, forceRefresh = true) },
                onReserve = { backStack.add(ConfirmReservation(trip.id)) },
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }

    entry<ConfirmReservation> { key ->
        state.tripBy(key.tripId)?.let { trip ->
            ConfirmReservationScreen(
                trip = trip,
                onConfirm = {
                    viewModel.reserveSeat(trip.id)?.let { reservationId ->
                        backStack.add(PaymentMethodChoice(reservationId))
                    }
                },
                onCancel = { backStack.removeLastOrNull() },
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }

    entry<PaymentMethodChoice> { key ->
        val reservation = state.reservationBy(key.reservationId)
        val amount = reservation?.let { state.tripBy(it.tripId)?.price } ?: 0
        PaymentMethodScreen(
            amount = amount,
            onMethodSelected = { method ->
                viewModel.selectPaymentMethod(key.reservationId, method)
                if (method == PaymentMethod.TRANSFER) {
                    backStack.add(TransferPayment(key.reservationId))
                } else {
                    backStack.add(PaymentOutcome(key.reservationId))
                }
            },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<TransferPayment> { key ->
        val payment = state.paymentFor(key.reservationId)
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.attachReceipt(key.reservationId, uri.toString())
        }
        TransferPaymentScreen(
            amount = payment?.amount ?: 0,
            reservationId = key.reservationId,
            receiptName = payment?.receiptName,
            receiptPath = payment?.receiptPath,
            importInProgress = state.receiptImportInProgress,
            receiptError = state.receiptError,
            onPickReceipt = {
                viewModel.clearReceiptError()
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onSubmit = {
                viewModel.submitReceipt(key.reservationId)
                backStack.add(PaymentOutcome(key.reservationId))
            },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<PaymentOutcome> { key ->
        val reservation = state.reservationBy(key.reservationId)
        val payment = state.paymentFor(key.reservationId)
        if (reservation != null && payment != null) {
            PaymentOutcomeScreen(
                reservation = reservation,
                payment = payment,
                routeName = state.tripBy(reservation.tripId)?.route?.name.orEmpty(),
                onOpenReservations = {
                    backToHome()
                    backStack.add(StudentReservations)
                },
                onBackHome = ::backToHome
            )
        }
    }

    entry<StudentReservations> {
        StudentReservationsScreen(
            rows = state.reservationRows(),
            onReservationSelected = { backStack.add(ReservationDetail(it)) },
            onBack = { backStack.removeLastOrNull() }
        )
    }

    entry<ReservationDetail> { key ->
        state.reservationRow(key.reservationId)?.let { row ->
            ReservationDetailScreen(
                row = row,
                passAvailable = state.availablePassFor(key.reservationId) != null,
                onOpenPass = { backStack.add(StudentPass(key.reservationId)) },
                onCompletePayment = { backStack.add(PaymentMethodChoice(key.reservationId)) },
                onCancelReservation = {
                    viewModel.cancelReservation(key.reservationId)
                    backStack.removeLastOrNull()
                },
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }

    entry<StudentPass> { key ->
        val pass = state.availablePassFor(key.reservationId)
        val trip = pass
            ?.let { state.reservationBy(it.reservationId) }
            ?.let { state.tripBy(it.tripId) }
        StudentPassScreen(
            pass = pass,
            trip = trip,
            studentName = user?.name.orEmpty(),
            onBack = { backStack.removeLastOrNull() }
        )
    }
}

private fun StudentUiState.reservationRows(): List<ReservationRow> =
    orderedReservations.map { reservation ->
        ReservationRow(
            reservation = reservation,
            trip = tripBy(reservation.tripId),
            payment = paymentFor(reservation.id)
        )
    }

private fun StudentUiState.reservationRow(reservationId: String): ReservationRow? =
    reservationBy(reservationId)?.let { reservation ->
        ReservationRow(
            reservation = reservation,
            trip = tripBy(reservation.tripId),
            payment = paymentFor(reservation.id)
        )
    }
