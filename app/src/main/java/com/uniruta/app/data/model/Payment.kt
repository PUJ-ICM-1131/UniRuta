package com.uniruta.app.data.model

enum class PaymentMethod(val label: String, val description: String) {
    TRANSFER(
        label = "Transferencia",
        description = "Realiza la transferencia y adjunta el comprobante para revisión."
    ),
    CASH_POINT(
        label = "Efectivo en punto",
        description = "Paga presencialmente en un punto habilitado. Tu reserva permanecerá pendiente hasta la confirmación."
    ),
    CASH_BOARDING(
        label = "Efectivo al abordar",
        description = "Paga al conductor antes de registrar tu abordaje."
    )
}

enum class PaymentStatus(val label: String) {
    PENDING("Pendiente"),
    UNDER_REVIEW("En revisión"),
    CONFIRMED("Confirmado"),
    REJECTED("Rechazado")
}

enum class RejectionReason(val label: String) {
    UNREADABLE_RECEIPT("Comprobante ilegible"),
    WRONG_AMOUNT("Valor incorrecto"),
    OTHER("Otro")
}

data class Payment(
    val id: String,
    val reservationId: String,
    val amount: Int,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val receiptName: String? = null,
    val receiptPath: String? = null,
    val rejectionReason: RejectionReason? = null
)
