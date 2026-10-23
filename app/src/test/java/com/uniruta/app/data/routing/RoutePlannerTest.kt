package com.uniruta.app.data.routing

import com.uniruta.app.data.mock.MockTrips
import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.RoutePath
import com.uniruta.app.data.model.RoutePathSource
import com.uniruta.app.data.model.Stop
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePlannerTest {

    private val route: Route = MockTrips.all.first { it.id == "trip-001" }.route

    @Test
    fun everyMockStopHasAValidLocation() {
        MockTrips.all.map { it.route }.distinctBy { it.id }.forEach { mockRoute ->
            assertTrue(mockRoute.name, mockRoute.stops.all { it.location?.isValid == true })
            assertTrue(mockRoute.isMappable)
        }
    }

    @Test
    fun successfulRouteIsCachedAndMarkedAsCalculated() = runTest {
        var calls = 0
        val calculated = RoutePath(
            points = listOf(GeoPoint(4.86, -74.06), GeoPoint(4.75, -74.04), GeoPoint(4.62, -74.06)),
            distanceMeters = 31_000.0,
            durationSeconds = 2_700.0,
            source = RoutePathSource.OPEN_ROUTE_SERVICE
        )
        val planner = RoutePlanner { waypoints ->
            calls++
            assertEquals(route.stops.size, waypoints.size)
            DirectionsResult.Success(calculated)
        }

        val first = planner.pathFor(route)
        val second = planner.pathFor(route)

        assertEquals(RoutePathState.Ready(calculated), first)
        assertEquals(first, second)
        assertEquals(1, calls)
    }

    @Test
    fun failureFallsBackToApproximatePathWithNotice() = runTest {
        val planner = RoutePlanner { DirectionsResult.Failure(DirectionsError.NETWORK) }

        val state = planner.pathFor(route) as RoutePathState.Ready

        assertEquals(RoutePathSource.APPROXIMATE, state.path.source)
        assertFalse(state.path.isCalculated)
        assertEquals(route.stops.mapNotNull { it.location }, state.path.points)
        assertEquals(null, state.path.distanceMeters)
        assertTrue(state.notice!!.startsWith(DirectionsError.NETWORK.message))
        assertTrue(state.retryable)
    }

    @Test
    fun failuresAreNotCached() = runTest {
        var calls = 0
        val planner = RoutePlanner {
            calls++
            DirectionsResult.Failure(DirectionsError.RATE_LIMITED)
        }

        planner.pathFor(route)
        planner.pathFor(route)

        assertEquals(2, calls)
    }

    @Test
    fun routeWithoutLocatedStopsIsUnavailable() = runTest {
        val adminRoute = Route(
            id = "route-mock-101",
            name = "Ruta nueva",
            origin = "Origen",
            destination = "Destino",
            stops = listOf(Stop("s1", "Parada 1", 1), Stop("s2", "Parada 2", 2))
        )
        val planner = RoutePlanner { error("No debe consultarse la API") }

        assertTrue(planner.pathFor(adminRoute) is RoutePathState.Unavailable)
    }

    @Test
    fun clientWithoutConfiguredKeyDoesNotCallTheNetwork() = runTest {
        val client = OpenRouteServiceClient(apiKey = "DEFAULT_API_KEY", baseUrl = "http://127.0.0.1:9")

        val result = client.directions(listOf(GeoPoint(4.86, -74.06), GeoPoint(4.62, -74.06)))

        assertEquals(DirectionsResult.Failure(DirectionsError.MISSING_KEY), result)
    }
}
