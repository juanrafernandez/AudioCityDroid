package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.PointsActionType
import com.jrlabs.audiocity.domain.model.PointsTransaction
import com.jrlabs.audiocity.domain.model.UserLevel
import com.jrlabs.audiocity.domain.model.UserPointsStats
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for gamification points service.
 * Handles user points, levels, and rewards.
 */
interface PointsService {

    /**
     * Current user statistics including points and level.
     */
    val stats: StateFlow<UserPointsStats>

    /**
     * History of all points transactions.
     */
    val transactions: StateFlow<List<PointsTransaction>>

    /**
     * Flow that emits when user levels up.
     */
    val recentLevelUp: StateFlow<UserLevel?>

    /**
     * Awards points for completing a route.
     * @param routeId The ID of the completed route.
     * @param routeName The name of the route.
     * @param completionPercentage The percentage of stops visited (100 = full completion).
     */
    fun awardPointsForCompletingRoute(
        routeId: String,
        routeName: String,
        completionPercentage: Int
    )

    /**
     * Awards points for creating a route.
     * Points vary based on number of stops.
     * @param routeId The ID of the created route.
     * @param routeName The name of the route.
     * @param numStops The number of stops in the route.
     */
    fun awardPointsForCreatingRoute(
        routeId: String,
        routeName: String,
        numStops: Int
    )

    /**
     * Awards points for publishing a route.
     * @param routeId The ID of the published route.
     * @param routeName The name of the route.
     */
    fun awardPointsForPublishingRoute(routeId: String, routeName: String)

    /**
     * Awards points when someone else uses your route.
     * @param routeId The ID of the route.
     * @param routeName The name of the route.
     */
    fun awardPointsForRouteUsedByOther(routeId: String, routeName: String)

    /**
     * Clears the recent level up notification.
     */
    fun clearLevelUpNotification()

    /**
     * Resets all points (for testing/debug).
     */
    fun resetPoints()
}
