package com.uniruta.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniruta.app.data.model.Route
import com.uniruta.app.ui.components.CardDivider
import com.uniruta.app.ui.components.EmptyState
import com.uniruta.app.ui.components.MetaItem
import com.uniruta.app.ui.components.StatusChip
import com.uniruta.app.ui.components.StatusTone
import com.uniruta.app.ui.components.UniRutaCard
import com.uniruta.app.ui.components.UniRutaFab
import com.uniruta.app.ui.components.UniRutaScreen
import com.uniruta.app.ui.theme.UniRutaIcons
import com.uniruta.app.ui.theme.UniRutaTheme
import com.uniruta.app.ui.theme.ValueStyle

@Composable
fun AdminRoutesScreen(
    routes: List<Route>,
    onRouteSelected: (String) -> Unit,
    onCreateRoute: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniRutaScreen(
        title = "Rutas",
        onBack = onBack,
        modifier = modifier,
        floatingAction = { UniRutaFab(text = "Nueva ruta", icon = UniRutaIcons.Plus, onClick = onCreateRoute) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 104.dp)
    ) {
        if (routes.isEmpty()) {
            EmptyState(icon = UniRutaIcons.Route, message = "Aún no hay rutas registradas.")
        }
        routes.sortedByDescending { it.active }.forEach { route ->
            RouteCard(route = route, onClick = { onRouteSelected(route.id) })
        }
    }
}

@Composable
private fun RouteCard(
    route: Route,
    onClick: () -> Unit
) {
    UniRutaCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalSpacing = 8.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (route.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            StatusChip(
                label = if (route.active) "Activa" else "Inactiva",
                tone = if (route.active) StatusTone.POSITIVE else StatusTone.NEUTRAL
            )
        }
        MetaItem(icon = UniRutaIcons.MapPin, text = "Origen: ${route.origin}")
        MetaItem(icon = UniRutaIcons.Flag, text = "Destino: ${route.destination}")
        CardDivider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (route.stops.size == 1) "1 parada" else "${route.stops.size} paradas",
                style = ValueStyle,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = UniRutaIcons.ChevronRight,
                contentDescription = null,
                tint = UniRutaTheme.colors.mutedIcon,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
