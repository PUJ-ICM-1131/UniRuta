package com.uniruta.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.uniruta.app.data.model.AnomalyType
import com.uniruta.app.data.model.PaymentMethod
import com.uniruta.app.data.model.VehicleStatus
import com.uniruta.app.ui.theme.UniRutaIcons

fun PaymentMethod.icon(): ImageVector = when (this) {
    PaymentMethod.TRANSFER -> UniRutaIcons.Bank
    PaymentMethod.CASH_POINT -> UniRutaIcons.Store
    PaymentMethod.CASH_BOARDING -> UniRutaIcons.Cash
}

fun PaymentMethod.tone(): StatusTone = when (this) {
    PaymentMethod.TRANSFER -> StatusTone.INFO
    PaymentMethod.CASH_POINT -> StatusTone.WARNING
    PaymentMethod.CASH_BOARDING -> StatusTone.POSITIVE
}

fun AnomalyType.icon(): ImageVector = when (this) {
    AnomalyType.HARD_BRAKING -> UniRutaIcons.AlertCircle
    AnomalyType.HARD_ACCELERATION -> UniRutaIcons.Activity
    AnomalyType.SHARP_TURN -> UniRutaIcons.Turn
}

fun VehicleStatus.iconTone(): StatusTone = when (this) {
    VehicleStatus.ACTIVE -> StatusTone.POSITIVE
    VehicleStatus.INACTIVE -> StatusTone.NEUTRAL
    VehicleStatus.MAINTENANCE -> StatusTone.WARNING
}
