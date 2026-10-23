package com.uniruta.app.viewmodel

import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.RejectionReason
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.routing.DirectionsError
import com.uniruta.app.data.routing.DirectionsResult
import com.uniruta.app.data.routing.RoutePlanner
import com.uniruta.app.data.storage.CaptureTarget
import com.uniruta.app.data.storage.ImageResult
import com.uniruta.app.data.storage.ImageStore
import com.uniruta.app.data.storage.StoredImage
import com.uniruta.app.notifications.AppNotification
import com.uniruta.app.notifications.AppNotifier
import com.uniruta.app.notifications.NotificationDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScenarioNotificationsTest {

    private class RecordingNotifier : AppNotifier {
        val sent = mutableListOf<AppNotification>()
        override fun notify(notification: AppNotification) {
            sent += notification
        }
    }

    private class FakeImageStore : ImageStore {
        val cleared = mutableListOf<String>()
        override suspend fun importImage(sourceUri: String, folder: String, baseName: String): ImageResult =
            ImageResult.Success(StoredImage("comprobante.png", "/files/$folder/$baseName.png", 1024))
        override fun createCaptureTarget(folder: String, baseName: String): CaptureTarget? = null
        override suspend fun confirmCapture(target: CaptureTarget): ImageResult = ImageResult.Failure("sin cámara")
        override suspend fun delete(path: String) = Unit
        override suspend fun clear(folder: String) {
            cleared += folder
        }
    }

    private val notifier = RecordingNotifier()
    private val images = FakeImageStore()
    private val planner = RoutePlanner { DirectionsResult.Failure(DirectionsError.MISSING_KEY) }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reservingASeatNotifiesOnceAndNeverDuplicatesTheReservation() {
        val viewModel = StudentViewModel(planner, images, notifier)
        val seatsBefore = viewModel.uiState.value.tripBy("trip-002")!!.availableSeats

        val first = viewModel.reserveSeat("trip-002")
        val second = viewModel.reserveSeat("trip-002")

        assertEquals(first, second)
        assertEquals(seatsBefore - 1, viewModel.uiState.value.tripBy("trip-002")!!.availableSeats)
        assertEquals(1, notifier.sent.size)
        assertEquals("Reserva creada", notifier.sent.single().title)
        assertEquals(NotificationDestination.RESERVATION_DETAIL, notifier.sent.single().target.destination)
        assertEquals(first, notifier.sent.single().target.entityId)
        assertTrue(viewModel.uiState.value.hasScenarioChanges)
    }

    @Test
    fun fullTripsCannotBeReservedAndCancellingNeverExceedsCapacity() {
        val viewModel = StudentViewModel(planner, images, notifier)

        assertNull(viewModel.reserveSeat("trip-003"))
        assertEquals(0, viewModel.uiState.value.tripBy("trip-003")!!.availableSeats)

        val reservationId = viewModel.reserveSeat("trip-002")!!
        assertEquals(4, viewModel.uiState.value.tripBy("trip-002")!!.availableSeats)
        viewModel.cancelReservation(reservationId)
        viewModel.cancelReservation(reservationId)

        val trip = viewModel.uiState.value.tripBy("trip-002")!!
        assertEquals(5, trip.availableSeats)
        assertEquals(ReservationStatus.CANCELLED, viewModel.uiState.value.reservationBy(reservationId)?.status)
    }

    @Test
    fun submittingAReceiptNotifiesOnlyWhenTheStateChanges() {
        val viewModel = StudentViewModel(planner, images, notifier)
        val reservationId = viewModel.reserveSeat("trip-004")!!
        viewModel.selectPaymentMethod(reservationId, PaymentMethod.TRANSFER)
        notifier.sent.clear()

        viewModel.submitReceipt(reservationId)
        assertTrue(notifier.sent.isEmpty())

        viewModel.attachReceipt(reservationId, "content://media/picker/1")
        viewModel.submitReceipt(reservationId)
        viewModel.submitReceipt(reservationId)

        assertEquals(PaymentStatus.UNDER_REVIEW, viewModel.uiState.value.paymentFor(reservationId)?.status)
        assertEquals(listOf("Comprobante enviado"), notifier.sent.map { it.title })
    }

    @Test
    fun studentResetRestoresTheScenarioWithoutNotifyingAndRemovesDemoReceipts() {
        val viewModel = StudentViewModel(planner, images, notifier)
        viewModel.reserveSeat("trip-002")
        notifier.sent.clear()
        images.cleared.clear()

        viewModel.reset()

        assertFalse(viewModel.uiState.value.hasScenarioChanges)
        assertTrue(notifier.sent.isEmpty())
        assertEquals(listOf("receipts"), images.cleared)
    }

    @Test
    fun adminReviewNotifiesApprovalsAndRejections() {
        val viewModel = AdminViewModel(notifier)

        viewModel.approvePayment("pay-3201")
        viewModel.rejectPayment("pay-3001", RejectionReason.UNREADABLE_RECEIPT)

        assertEquals(listOf("Pago aprobado", "Pago rechazado"), notifier.sent.map { it.title })
        assertTrue(notifier.sent.all { it.target.destination == NotificationDestination.ADMIN_PAYMENT_DETAIL })
        assertTrue(notifier.sent.none { it.text.contains("8b9f41f6") })
        assertTrue(viewModel.uiState.value.hasScenarioChanges)
    }

    @Test
    fun adminRestrictionsStillBlockInvalidReviewsWithoutNotifying() {
        val viewModel = AdminViewModel(notifier)

        viewModel.rejectPayment("pay-3201", RejectionReason.OTHER)
        viewModel.approvePayment("pay-3101")
        viewModel.approvePayment("pay-3002")

        assertTrue(notifier.sent.isEmpty())
        assertFalse(viewModel.uiState.value.hasScenarioChanges)

        viewModel.approvePayment("pay-3201")
        viewModel.reset()
        assertFalse(viewModel.uiState.value.hasScenarioChanges)
    }
}
