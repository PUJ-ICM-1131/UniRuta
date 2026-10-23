package com.uniruta.app.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminViewModelTest {

    @Test
    fun editingARouteKeepsTheLocationOfExistingStops() {
        val viewModel = AdminViewModel()
        val route = viewModel.uiState.value.routeBy("route-chia")!!
        val names = route.orderedStops.map { it.name }

        val error = viewModel.saveRoute(
            routeId = route.id,
            name = route.name,
            origin = route.origin,
            destination = route.destination,
            stopNames = names + "Parada nueva"
        )

        val saved = viewModel.uiState.value.routeBy(route.id)!!
        assertNull(error)
        assertEquals(route.orderedStops.map { it.location }, saved.orderedStops.take(names.size).map { it.location })
        assertNull(saved.orderedStops.last().location)
        assertTrue(saved.isMappable)
    }
}
