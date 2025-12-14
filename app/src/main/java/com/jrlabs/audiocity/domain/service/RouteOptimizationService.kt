package com.jrlabs.audiocity.domain.service

import android.location.Location
import com.jrlabs.audiocity.domain.model.Stop

/**
 * Interface for route optimization service.
 * Calculates optimal route order based on user location.
 */
interface RouteOptimizationService {

    /**
     * Information about the nearest stop.
     */
    data class NearestStopInfo(
        val stop: Stop,
        val distanceMeters: Float,
        val isFirstInOrder: Boolean
    )

    /**
     * Checks if route optimization should be suggested.
     * @param stops The stops in original order.
     * @param userLocation The user's current location.
     * @return true if the nearest stop is not the first one.
     */
    fun shouldSuggestOptimization(
        stops: List<Stop>,
        userLocation: Location
    ): Boolean

    /**
     * Gets information about the nearest stop.
     * @param stops The stops to search.
     * @param userLocation The user's current location.
     */
    fun getNearestStopInfo(
        stops: List<Stop>,
        userLocation: Location
    ): NearestStopInfo?

    /**
     * Optimizes route starting from the nearest stop.
     * @param stops The original stops.
     * @param userLocation The user's current location.
     * @return Reordered stops starting from the nearest.
     */
    fun optimizeRoute(
        stops: List<Stop>,
        userLocation: Location
    ): List<Stop>

    /**
     * Calculates distance between two points.
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float
}
