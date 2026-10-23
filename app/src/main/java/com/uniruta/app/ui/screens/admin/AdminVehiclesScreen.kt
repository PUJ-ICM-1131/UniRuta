package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Vehicle
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.IconTile
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaFab
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.components.iconTone
import com.uniruta.app.ui.components.tone
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme

@Composable
fun AdminVehiclesScreen(
    vehicles: List<Vehicle>,
    onVehicleSelected: (String) -> Unit,
    onCreateVehicle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Vehículos",
        onBack = onBack,
        modifier = modifier,
        floatingAction = { UniRutaFab(text = "Nuevo vehículo", icon = UniRutaIcons.Plus, onClick = onCreateVehicle) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 104.dp)
    ) {
        if (vehicles.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Truck, message = "Aún no hay vehículos registrados.")
        }
        vehicles.forEach { vehicle ->
            UniRutaCard(
                onClick = { onVehicleSelected(vehicle.id) },
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconTile(icon = UniRutaIcons.Truck, tone = vehicle.status.iconTone())
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = vehicle.plate,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(label = vehicle.status.label, tone = vehicle.status.tone())
                        }
                        Text(
                            text = "Capacidad: ${vehicle.capacity} puestos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = UniRutaIcons.ChevronRight,
                        contentDescription = null,
                        tint = UniRutaTheme.colors.mutedIcon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
