package com.uniruta.app.data.geo

import com.uniruta.app.data.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoMathTest {

    private val start = GeoPoint(4.0, -74.0)
    private val end = GeoPoint(4.0, -73.99)

    @Test
    fun distanceBetweenPointsUsesHaversine() {
        val meters = distanceMeters(start, end)

        assertEquals(1109.0, meters, 3.0)
    }

    @Test
    fun pointAtFractionInterpolatesAlongThePath() {
        val path = listOf(start, end)

        assertEquals(start, path.pointAtFraction(0.0))
        assertEquals(end, path.pointAtFraction(1.0))
        val middle = path.pointAtFraction(0.5)!!
        assertEquals(4.0, middle.latitude, 1e-9)
        assertEquals(-73.995, middle.longitude, 1e-6)
    }

    @Test
    fun pointAtFractionClampsOutOfRangeValues() {
        val path = listOf(start, end)

        assertEquals(start, path.pointAtFraction(-1.0))
        assertEquals(end, path.pointAtFraction(2.0))
    }

    @Test
    fun emptyPathHasNoPoint() {
        assertNull(emptyList<GeoPoint>().pointAtFraction(0.5))
    }

    @Test
    fun invalidCoordinatesAreDetected() {
        assertTrue(GeoPoint(4.6, -74.06).isValid)
        assertTrue(!GeoPoint(-74.06, 200.0).isValid)
        assertTrue(!GeoPoint(Double.NaN, 0.0).isValid)
    }
}
