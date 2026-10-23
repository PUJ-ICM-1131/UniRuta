package com.uniruta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.PassStatus
import com.uniruta.app.data.model.PaymentStatus
import com.uniruta.app.data.model.ReservationStatus
import com.uniruta.app.data.model.TrackingStatus
import com.uniruta.app.data.model.TripStatus
import com.uniruta.app.data.model.VehicleStatus
import com.uniruta.app.ui.theme.UniRutaTheme

enum class StatusTone { NEUTRAL, INFO, POSITIVE, WARNING, NEGATIVE }

@Immutable
data class ToneColors(
    val container: Color,
    val content: Color
)

@Composable
fun StatusTone.colors(): ToneColors {
    val extended = UniRutaTheme.colors
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        StatusTone.POSITIVE -> ToneColors(extended.successContainer, extended.onSuccessContainer)
        StatusTone.WARNING -> ToneColors(extended.warningContainer, extended.onWarningContainer)
        StatusTone.NEGATIVE -> ToneColors(scheme.errorContainer, scheme.onErrorContainer)
        StatusTone.INFO -> ToneColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        StatusTone.NEUTRAL -> ToneColors(extended.progressTrack, scheme.onSurfaceVariant)
    }
}

@Composable
fun StatusChip(
    label: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val colors = tone.colors()
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 28.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colors.content, CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun ReservationStatus.tone(): StatusTone = when (this) {
    ReservationStatus.PENDING -> StatusTone.WARNING
    ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED -> StatusTone.POSITIVE
    ReservationStatus.BOARDED -> StatusTone.INFO
    ReservationStatus.CANCELLED -> StatusTone.NEGATIVE
}

fun PaymentStatus.tone(): StatusTone = when (this) {
    PaymentStatus.PENDING, PaymentStatus.UNDER_REVIEW -> StatusTone.WARNING
    PaymentStatus.CONFIRMED -> StatusTone.POSITIVE
    PaymentStatus.REJECTED -> StatusTone.NEGATIVE
}

fun PassStatus.tone(): StatusTone = when (this) {
    PassStatus.ACTIVE -> StatusTone.POSITIVE
    PassStatus.USED -> StatusTone.NEUTRAL
    PassStatus.INVALID -> StatusTone.NEGATIVE
}

fun TripStatus.tone(): StatusTone = when (this) {
    TripStatus.SCHEDULED -> StatusTone.INFO
    TripStatus.IN_PROGRESS -> StatusTone.POSITIVE
    TripStatus.COMPLETED -> StatusTone.NEUTRAL
    TripStatus.CANCELLED -> StatusTone.NEGATIVE
}

fun TrackingStatus.tone(): StatusTone = when (this) {
    TrackingStatus.ACTIVE -> StatusTone.POSITIVE
    TrackingStatus.SEARCHING -> StatusTone.INFO
    TrackingStatus.INACTIVE, TrackingStatus.PAUSED -> StatusTone.NEUTRAL
    TrackingStatus.PERMISSION_DENIED, TrackingStatus.DISABLED -> StatusTone.WARNING
    TrackingStatus.UNAVAILABLE, TrackingStatus.ERROR -> StatusTone.NEGATIVE
}

fun VehicleStatus.tone(): StatusTone = when (this) {
    VehicleStatus.ACTIVE -> StatusTone.POSITIVE
    VehicleStatus.INACTIVE -> StatusTone.NEUTRAL
    VehicleStatus.MAINTENANCE -> StatusTone.WARNING
}
