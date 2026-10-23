package com.uniruta.app.notifications

import com.uniruta.app.data.model.Incident
import com.uniruta.app.data.model.RejectionReason
import com.uniruta.app.data.model.Trip
import com.uniruta.app.ui.format.asShortText
import com.uniruta.app.ui.format.asText

object NotificationMessages {

    fun reservationCreated(reservationId: String, trip: Trip) = AppNotification(
        key = "reservation_created:$reservationId",
        channel = NotificationChannelSpec.RESERVATIONS,
        title = "Reserva creada",
        text = "${trip.route.name} · ${trip.date.asShortText()}, ${trip.departureTime.asText()} · " +
            "Elige el método de pago para completarla.",
        target = NotificationTarget(NotificationDestination.RESERVATION_DETAIL, reservationId)
    )

    fun receiptSubmitted(reservationId: String, trip: Trip?) = AppNotification(
        key = "receipt_submitted:$reservationId",
        channel = NotificationChannelSpec.RESERVATIONS,
        title = "Comprobante enviado",
        text = listOfNotNull(trip?.route?.name, "Tu pago quedó en revisión.").joinToString(" · "),
        target = NotificationTarget(NotificationDestination.RESERVATION_DETAIL, reservationId)
    )

    fun passAvailable(reservationId: String, trip: Trip?) = AppNotification(
        key = "pass_available:$reservationId",
        channel = NotificationChannelSpec.RESERVATIONS,
        title = "Pase disponible",
        text = listOfNotNull(trip?.route?.name, "Ya puedes presentar tu pase al abordar.").joinToString(" · "),
        target = NotificationTarget(NotificationDestination.STUDENT_PASS, reservationId)
    )

    fun tripStarted(trip: Trip) = AppNotification(
        key = "trip_started:${trip.id}",
        channel = NotificationChannelSpec.TRIPS,
        title = "Recorrido iniciado",
        text = "${trip.route.name} · Salida ${trip.departureTime.asText()} · " +
            "El seguimiento funciona mientras UniRuta está abierta.",
        target = NotificationTarget(NotificationDestination.ACTIVE_TRIP, trip.id)
    )

    fun tripFinished(trip: Trip, boardedCount: Int, passengerCount: Int) = AppNotification(
        key = "trip_finished:${trip.id}",
        channel = NotificationChannelSpec.TRIPS,
        title = "Recorrido finalizado",
        text = "${trip.route.name} · $boardedCount de $passengerCount pasajeros abordaron. " +
            "La ubicación y los sensores se detuvieron.",
        target = NotificationTarget(NotificationDestination.TRIP_FINISHED, trip.id)
    )

    fun incidentReported(incident: Incident, trip: Trip?) = AppNotification(
        key = "incident_reported:${incident.id}",
        channel = NotificationChannelSpec.TRIPS,
        title = "Novedad registrada",
        text = listOfNotNull(
            incident.type.label,
            trip?.route?.name,
            if (incident.evidencePath != null) "Con fotografía" else "Sin fotografía"
        ).joinToString(" · "),
        target = NotificationTarget(NotificationDestination.ACTIVE_TRIP, incident.tripId)
    )

    fun paymentApproved(paymentId: String, reservationId: String, trip: Trip?) = AppNotification(
        key = "payment_reviewed:$paymentId",
        channel = NotificationChannelSpec.ADMINISTRATION,
        title = "Pago aprobado",
        text = listOfNotNull("Reserva $reservationId", trip?.route?.name, "El pase quedó activo.")
            .joinToString(" · "),
        target = NotificationTarget(NotificationDestination.ADMIN_PAYMENT_DETAIL, paymentId)
    )

    fun paymentRejected(paymentId: String, reservationId: String, reason: RejectionReason) = AppNotification(
        key = "payment_reviewed:$paymentId",
        channel = NotificationChannelSpec.ADMINISTRATION,
        title = "Pago rechazado",
        text = "Reserva $reservationId · Motivo: ${reason.label}.",
        target = NotificationTarget(NotificationDestination.ADMIN_PAYMENT_DETAIL, paymentId)
    )
}
