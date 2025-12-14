package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.model.Trip
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for offline caching service.
 * Handles downloading routes and maps for offline use.
 */
interface OfflineCacheService {

    /**
     * Status of a download operation.
     */
    sealed class DownloadStatus {
        data object Idle : DownloadStatus()
        data class Downloading(
            val progress: Float,
            val currentRouteName: String
        ) : DownloadStatus()
        data object Completed : DownloadStatus()
        data class Failed(val error: String) : DownloadStatus()
    }

    /**
     * Cached route data.
     */
    data class CachedRoute(
        val tripId: String,
        val route: Route,
        val stops: List<Stop>,
        val sizeBytes: Long
    )

    /**
     * Current download status.
     */
    val downloadStatus: StateFlow<DownloadStatus>

    /**
     * All cached routes.
     */
    val cachedRoutes: StateFlow<List<CachedRoute>>

    /**
     * Total cache size in bytes.
     */
    val totalCacheSizeBytes: StateFlow<Long>

    /**
     * Downloads all routes for a trip.
     * @param trip The trip to download routes for.
     */
    suspend fun downloadTrip(trip: Trip)

    /**
     * Downloads a single route.
     * @param routeId The ID of the route to download.
     * @param tripId The ID of the trip this route belongs to.
     */
    suspend fun downloadRoute(routeId: String, tripId: String)

    /**
     * Checks if a route is cached.
     * @param routeId The ID of the route.
     */
    fun isRouteCached(routeId: String): Boolean

    /**
     * Gets cached route data.
     * @param routeId The ID of the route.
     */
    fun getCachedRoute(routeId: String): CachedRoute?

    /**
     * Gets cached stops for a route.
     * @param routeId The ID of the route.
     */
    fun getCachedStops(routeId: String): List<Stop>?

    /**
     * Deletes cache for a trip.
     * @param tripId The ID of the trip.
     */
    fun deleteTripCache(tripId: String)

    /**
     * Clears all cache.
     */
    fun clearAllCache()

    /**
     * Estimates download size for a trip.
     * @param routeIds The route IDs to estimate.
     * @return Estimated size in bytes.
     */
    fun estimateDownloadSize(routeIds: List<String>): Long

    /**
     * Returns formatted cache size string.
     */
    fun getFormattedCacheSize(): String
}
