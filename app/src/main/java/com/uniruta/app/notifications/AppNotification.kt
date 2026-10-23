package com.uniruta.app.notifications

import com.uniruta.app.data.model.UserRole

enum class NotificationChannelSpec(
    val id: String,
    val title: String,
    val description: String
) {
    RESERVATIONS(
        id = "reservations_payments",
        title = "Reservas y pagos",
        description = "Reservas creadas, comprobantes enviados y pases disponibles."
    ),
    TRIPS(
        id = "driver_trips",
        title = "Recorridos",
        description = "Inicio y finalización de recorridos y novedades registradas."
    ),
    ADMINISTRATION(
        id = "payment_review",
        title = "Revisión de pagos",
        description = "Pagos aprobados o rechazados durante la revisión."
    )
}

enum class NotificationDestination(val role: UserRole) {
    STUDENT_RESERVATIONS(UserRole.STUDENT),
    RESERVATION_DETAIL(UserRole.STUDENT),
    STUDENT_PASS(UserRole.STUDENT),
    DRIVER_HOME(UserRole.DRIVER),
    ACTIVE_TRIP(UserRole.DRIVER),
    TRIP_FINISHED(UserRole.DRIVER),
    ADMIN_PAYMENTS(UserRole.ADMIN),
    ADMIN_PAYMENT_DETAIL(UserRole.ADMIN)
}

data class NotificationTarget(
    val destination: NotificationDestination,
    val entityId: String? = null
) {
    val role: UserRole
        get() = destination.role
}

data class AppNotification(
    val key: String,
    val channel: NotificationChannelSpec,
    val title: String,
    val text: String,
    val target: NotificationTarget
)

fun interface AppNotifier {

    fun notify(notification: AppNotification)

    companion object {
        val None = AppNotifier { }
    }
}
