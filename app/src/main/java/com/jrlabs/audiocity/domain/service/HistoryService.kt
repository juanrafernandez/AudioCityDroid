package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.HistoryGroup
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.RouteHistoryEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for route history service.
 * Tracks completed routes and statistics.
 */
interface HistoryService {

    /**
     * All history entries.
     */
    val historyEntries: StateFlow<List<RouteHistoryEntry>>

    /**
     * History grouped by date.
     */
    val groupedHistory: StateFlow<List<HistoryGroup>>

    /**
     * Total routes completed.
     */
    val totalRoutesCompleted: StateFlow<Int>

    /**
     * Total distance walked in kilometers.
     */
    val totalDistanceKm: StateFlow<Double>

    /**
     * Total time spent in minutes.
     */
    val totalTimeMinutes: StateFlow<Int>

    /**
     * Records a completed route (legacy method).
     * @param route The completed route.
     * @param stopsVisited Number of stops the user visited.
     * @param durationMinutes Actual time spent on the route.
     * @param distanceKm Actual distance walked.
     * @param pointsEarned Points awarded for this completion.
     */
    fun addToHistory(
        route: Route,
        stopsVisited: Int,
        durationMinutes: Int,
        distanceKm: Double,
        pointsEarned: Int
    )

    /**
     * Records when a route is started.
     */
    fun recordRouteStarted(
        routeId: String,
        routeName: String,
        routeCity: String,
        totalStops: Int,
        distanceKm: Double
    )

    /**
     * Records when a route is ended (completed or cancelled).
     */
    fun recordRouteEnded(
        stopsVisited: Int,
        wasCompleted: Boolean
    )

    /**
     * Check if there's an active route being tracked.
     */
    fun hasActiveRoute(): Boolean

    /**
     * Clears all history.
     */
    fun clearHistory()

    /**
     * Gets the most recent entry.
     */
    fun getMostRecentEntry(): RouteHistoryEntry?
}
