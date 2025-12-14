package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.Stop

/**
 * Interface for calculating real walking distances between stops.
 * Uses Google Directions API for accurate walking routes.
 */
interface RouteDistanceCalculator {

    /**
     * Result of a distance calculation.
     */
    data class DistanceResult(
        val distanceMeters: Int,
        val durationSeconds: Int,
        val isEstimate: Boolean = false // True if using Euclidean fallback
    )

    /**
     * Calculates walking distance and time between two stops.
     * @param from Starting stop.
     * @param to Destination stop.
     * @return Distance result with meters and seconds.
     */
    suspend fun calculateWalkingDistance(
        from: Stop,
        to: Stop
    ): DistanceResult

    /**
     * Calculates walking distance from coordinates.
     * @return Distance result with meters and seconds.
     */
    suspend fun calculateWalkingDistance(
        fromLat: Double, fromLon: Double,
        toLat: Double, toLon: Double
    ): DistanceResult

    /**
     * Calculates total route distance and duration.
     * @param stops All stops in order.
     * @return Total distance and duration for the entire route.
     */
    suspend fun calculateTotalRouteDistance(
        stops: List<Stop>
    ): DistanceResult

    /**
     * Calculates Euclidean (straight line) distance as fallback.
     * @return Distance in meters.
     */
    fun calculateEuclideanDistance(
        fromLat: Double, fromLon: Double,
        toLat: Double, toLon: Double
    ): Float
}
