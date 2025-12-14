package com.jrlabs.audiocity.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for getting walking directions between points using Valhalla routing engine.
 * Uses the free OpenStreetMap Valhalla server.
 */
@Singleton
class RoutingService @Inject constructor() {

    companion object {
        private const val TAG = "RoutingService"
        // Valhalla server hosted by OpenStreetMap Germany (free, no API key)
        private const val VALHALLA_BASE_URL = "https://valhalla1.openstreetmap.de/route"
    }

    /**
     * Get walking route between two points using Valhalla.
     * Returns a list of LatLng points representing the path.
     */
    suspend fun getRouteBetweenTwoPoints(start: LatLng, end: LatLng): Result<List<LatLng>> = withContext(Dispatchers.IO) {
        try {
            // Build Valhalla JSON request
            val requestJson = JSONObject().apply {
                put("locations", JSONArray().apply {
                    put(JSONObject().apply {
                        put("lat", start.latitude)
                        put("lon", start.longitude)
                    })
                    put(JSONObject().apply {
                        put("lat", end.latitude)
                        put("lon", end.longitude)
                    })
                })
                put("costing", "pedestrian")
                put("directions_options", JSONObject().apply {
                    put("units", "kilometers")
                })
            }

            val encodedJson = URLEncoder.encode(requestJson.toString(), "UTF-8")
            val url = "$VALHALLA_BASE_URL?json=$encodedJson"

            Log.d(TAG, "Fetching route: ${start.latitude},${start.longitude} -> ${end.latitude},${end.longitude}")

            val response = URL(url).readText()
            val json = JSONObject(response)

            // Check for errors
            if (json.has("error")) {
                val error = json.getString("error")
                Log.e(TAG, "Valhalla error: $error")
                return@withContext Result.failure(Exception("Valhalla error: $error"))
            }

            // Parse the shape (encoded polyline)
            val trip = json.getJSONObject("trip")
            val legs = trip.getJSONArray("legs")

            val routePoints = mutableListOf<LatLng>()

            for (legIndex in 0 until legs.length()) {
                val leg = legs.getJSONObject(legIndex)
                val shape = leg.getString("shape")

                // Decode the polyline
                val decodedPoints = decodePolyline(shape)
                routePoints.addAll(decodedPoints)
            }

            Log.d(TAG, "Route segment loaded with ${routePoints.size} points")
            Result.success(routePoints)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get route: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Decode a polyline string into a list of LatLng points.
     * Valhalla uses precision 6 (divide by 1e6).
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            // Valhalla uses precision 6
            val latLng = LatLng(lat / 1e6, lng / 1e6)
            poly.add(latLng)
        }

        return poly
    }

    /**
     * Get complete walking route from user location through all stops.
     * Calculates each segment sequentially to avoid rate limiting.
     *
     * @param userLocation Current user location (starting point)
     * @param stops List of stops in order to visit
     * @return Combined list of all route points
     */
    suspend fun getCompleteWalkingRoute(
        userLocation: LatLng,
        stops: List<LatLng>
    ): Result<List<LatLng>> = withContext(Dispatchers.IO) {
        if (stops.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No stops provided"))
        }

        try {
            // Build list of all segments to calculate
            // Segment 0: User -> Stop 1
            // Segment 1: Stop 1 -> Stop 2
            // etc.
            val allPoints = listOf(userLocation) + stops
            val segments = mutableListOf<Pair<LatLng, LatLng>>()

            for (i in 0 until allPoints.size - 1) {
                segments.add(Pair(allPoints[i], allPoints[i + 1]))
            }

            Log.d(TAG, "Calculating ${segments.size} route segments sequentially...")

            // Combine all segments into one route
            val combinedRoute = mutableListOf<LatLng>()

            // Calculate segments sequentially with small delay to avoid rate limiting
            for ((index, segment) in segments.withIndex()) {
                val (start, end) = segment

                // Small delay between requests to avoid rate limiting (except first)
                if (index > 0) {
                    delay(200)
                }

                Log.d(TAG, "Calculating segment $index of ${segments.size}...")

                val result = getRouteBetweenTwoPoints(start, end)

                result.fold(
                    onSuccess = { points ->
                        Log.d(TAG, "Segment $index: ${points.size} points")
                        // Skip the first point of subsequent segments to avoid duplicates
                        if (index == 0) {
                            combinedRoute.addAll(points)
                        } else if (points.isNotEmpty()) {
                            combinedRoute.addAll(points.drop(1))
                        }
                    },
                    onFailure = { error ->
                        Log.w(TAG, "Segment $index failed, using straight line: ${error.message}")
                        // Fallback: add straight line for this segment
                        if (index == 0) {
                            combinedRoute.add(start)
                        }
                        combinedRoute.add(end)
                    }
                )
            }

            Log.d(TAG, "Complete route calculated with ${combinedRoute.size} points")
            Result.success(combinedRoute)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate complete route: ${e.message}")
            Result.failure(e)
        }
    }
}
