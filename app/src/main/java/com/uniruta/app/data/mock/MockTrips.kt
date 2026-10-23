package com.uniruta.app.data.mock

import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.Route
import com.uniruta.app.data.model.Stop
import com.uniruta.app.data.model.Trip
import java.time.LocalDate
import java.time.LocalTime

object MockTrips {

    private const val DESTINATION = "Pontificia Universidad Javeriana"

    private val javerianaCampus = GeoPoint(4.628706, -74.063650)

    private val chiaRoute = Route(
        id = "route-chia",
        name = "Chía → Javeriana",
        origin = "Chía",
        destination = DESTINATION,
        stops = listOf(
            Stop("stop-ch-1", "Chía - Parque Central", 1, GeoPoint(4.861286, -74.060220)),
            Stop("stop-ch-2", "Puente del Común", 2, GeoPoint(4.863434, -74.029631)),
            Stop("stop-ch-3", "Autopista Norte - Calle 170", 3, GeoPoint(4.751641, -74.046356)),
            Stop("stop-ch-4", "Calle 100", 4, GeoPoint(4.683805, -74.057702)),
            Stop("stop-ch-5", "Javeriana - Carrera 7", 5, javerianaCampus)
        )
    )

    private val cedritosRoute = Route(
        id = "route-cedritos",
        name = "Cedritos → Javeriana",
        origin = "Cedritos",
        destination = DESTINATION,
        stops = listOf(
            Stop("stop-ce-1", "Cedritos - Calle 140", 1, GeoPoint(4.723606, -74.047271)),
            Stop("stop-ce-2", "Calle 127", 2, GeoPoint(4.704509, -74.054259)),
            Stop("stop-ce-3", "Calle 116", 3, GeoPoint(4.697981, -74.055329)),
            Stop("stop-ce-4", "Héroes", 4, GeoPoint(4.668121, -74.060326)),
            Stop("stop-ce-5", "Javeriana - Carrera 7", 5, javerianaCampus)
        )
    )

    private val subaRoute = Route(
        id = "route-suba",
        name = "Suba → Javeriana",
        origin = "Suba",
        destination = DESTINATION,
        stops = listOf(
            Stop("stop-su-1", "Suba - Centro", 1, GeoPoint(4.741013, -74.083865)),
            Stop("stop-su-2", "Portal Suba", 2, GeoPoint(4.746897, -74.094291)),
            Stop("stop-su-3", "Calle 80", 3, GeoPoint(4.685777, -74.080333)),
            Stop("stop-su-4", "Avenida Caracas", 4, GeoPoint(4.631028, -74.067982)),
            Stop("stop-su-5", "Javeriana - Carrera 7", 5, javerianaCampus)
        )
    )

    private val modeliaRoute = Route(
        id = "route-modelia",
        name = "Modelia → Javeriana",
        origin = "Modelia",
        destination = DESTINATION,
        stops = listOf(
            Stop("stop-mo-1", "Modelia - Avenida La Esperanza", 1, GeoPoint(4.667656, -74.122151)),
            Stop("stop-mo-2", "Avenida Boyacá", 2, GeoPoint(4.663054, -74.114360)),
            Stop("stop-mo-3", "Salitre", 3, GeoPoint(4.650967, -74.101709)),
            Stop("stop-mo-4", "Universidad Nacional", 4, GeoPoint(4.636989, -74.079345)),
            Stop("stop-mo-5", "Javeriana - Carrera 7", 5, javerianaCampus)
        )
    )

    private val today: LocalDate = LocalDate.now()
    private val tomorrow: LocalDate = today.plusDays(1)

    val all: List<Trip> = listOf(
        Trip(
            id = "trip-001",
            route = chiaRoute,
            date = today,
            departureTime = LocalTime.of(6, 30),
            availableSeats = 22,
            totalSeats = 25,
            vehiclePlate = "TUV-421",
            driverName = "Carlos Medina",
            estimatedDurationMinutes = 55,
            estimatedDistanceKm = 28.4,
            price = 8500
        ),
        Trip(
            id = "trip-002",
            route = cedritosRoute,
            date = today,
            departureTime = LocalTime.of(7, 0),
            availableSeats = 5,
            totalSeats = 20,
            vehiclePlate = "WXY-118",
            driverName = "Marcela Ríos",
            estimatedDurationMinutes = 40,
            estimatedDistanceKm = 12.6,
            price = 5500
        ),
        Trip(
            id = "trip-003",
            route = subaRoute,
            date = today,
            departureTime = LocalTime.of(6, 45),
            availableSeats = 0,
            totalSeats = 22,
            vehiclePlate = "KLP-337",
            driverName = "Andrés Lozano",
            estimatedDurationMinutes = 50,
            estimatedDistanceKm = 17.2,
            price = 6000
        ),
        Trip(
            id = "trip-004",
            route = modeliaRoute,
            date = tomorrow,
            departureTime = LocalTime.of(7, 15),
            availableSeats = 18,
            totalSeats = 25,
            vehiclePlate = "KLP-337",
            driverName = "Diana Castro",
            estimatedDurationMinutes = 45,
            estimatedDistanceKm = 15.8,
            price = 6000
        ),
        Trip(
            id = "trip-005",
            route = chiaRoute,
            date = tomorrow,
            departureTime = LocalTime.of(7, 30),
            availableSeats = 23,
            totalSeats = 25,
            vehiclePlate = "TUV-421",
            driverName = "Carlos Medina",
            estimatedDurationMinutes = 60,
            estimatedDistanceKm = 28.4,
            price = 8500
        )
    )
}
