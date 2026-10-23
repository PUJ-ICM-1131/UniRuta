package com.uniruta.app.data.routing

import com.uniruta.app.data.model.GeoPoint
import com.uniruta.app.data.model.RoutePathSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRouteServiceParserTest {

    private val sampleResponse = """
        {
          "type": "FeatureCollection",
          "bbox": [-74.06, 4.62, -74.05, 4.70],
          "features": [
            {
              "bbox": [-74.06, 4.62, -74.05, 4.70],
              "type": "Feature",
              "properties": {
                "segments": [{ "distance": 9123.4, "duration": 1260.5, "steps": [] }],
                "summary": { "distance": 9123.4, "duration": 1260.5 },
                "way_points": [0, 2]
              },
              "geometry": {
                "coordinates": [[-74.0577, 4.6838], [-74.0601, 4.6650], [-74.0636, 4.6287]],
                "type": "LineString"
              }
            }
          ],
          "metadata": { "service": "routing" }
        }
    """.trimIndent()

    @Test
    fun parsesGeometryConvertingLongitudeLatitudeOrder() {
        val result = OpenRouteServiceParser.parseRoute(sampleResponse)

        assertTrue(result is DirectionsResult.Success)
        val path = (result as DirectionsResult.Success).path
        assertEquals(3, path.points.size)
        assertEquals(GeoPoint(latitude = 4.6838, longitude = -74.0577), path.points.first())
        assertEquals(GeoPoint(latitude = 4.6287, longitude = -74.0636), path.points.last())
        assertEquals(9123.4, path.distanceMeters!!, 0.001)
        assertEquals(1260.5, path.durationSeconds!!, 0.001)
        assertEquals(RoutePathSource.OPEN_ROUTE_SERVICE, path.source)
    }

    @Test
    fun emptyFeatureCollectionMeansNoRoute() {
        val result = OpenRouteServiceParser.parseRoute("""{"type":"FeatureCollection","features":[]}""")

        assertEquals(DirectionsResult.Failure(DirectionsError.NO_ROUTE), result)
    }

    @Test
    fun malformedBodyIsInvalidResponse() {
        assertEquals(
            DirectionsResult.Failure(DirectionsError.INVALID_RESPONSE),
            OpenRouteServiceParser.parseRoute("<html>Bad gateway</html>")
        )
    }

    @Test
    fun geometryWithASinglePointIsInvalid() {
        val body = """{"features":[{"geometry":{"coordinates":[[-74.05, 4.68]]}}]}"""

        assertEquals(
            DirectionsResult.Failure(DirectionsError.INVALID_RESPONSE),
            OpenRouteServiceParser.parseRoute(body)
        )
    }

    @Test
    fun mapsHttpErrorsToDomainErrors() {
        assertEquals(DirectionsError.UNAUTHORIZED, OpenRouteServiceParser.errorFor(401, null))
        assertEquals(
            DirectionsError.UNAUTHORIZED,
            OpenRouteServiceParser.errorFor(403, """{"error":"Access to this API has been disallowed"}""")
        )
        assertEquals(
            DirectionsError.RATE_LIMITED,
            OpenRouteServiceParser.errorFor(429, """{"error":"Rate limit exceeded"}""")
        )
        assertEquals(
            DirectionsError.NO_ROUTE,
            OpenRouteServiceParser.errorFor(
                404,
                """{"error":{"code":2010,"message":"Could not find routable point"}}"""
            )
        )
        assertEquals(
            DirectionsError.NO_ROUTE,
            OpenRouteServiceParser.errorFor(400, """{"error":{"code":2009,"message":"Route not found"}}""")
        )
        assertEquals(DirectionsError.SERVER, OpenRouteServiceParser.errorFor(500, "not json"))
    }

    @Test
    fun requestBodyUsesLongitudeLatitudeOrder() {
        val body = OpenRouteServiceParser.requestBody(
            waypoints = listOf(GeoPoint(4.861286, -74.060220), GeoPoint(4.628706, -74.063650)),
            snapRadiusMeters = 1000
        )

        assertTrue(body.contains("[-74.06022,4.861286]"))
        assertTrue(body.contains("[-74.06365,4.628706]"))
        assertTrue(body.contains("\"radiuses\":[1000,1000]"))
        assertTrue(body.contains("\"instructions\":false"))
    }
}
