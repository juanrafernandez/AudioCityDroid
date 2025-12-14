package com.jrlabs.audiocity.domain.repository

import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.common.Result

/**
 * Repository interface for Route operations.
 * Follows Interface Segregation Principle (ISP) - only route-related operations.
 * Allows for different implementations (Firebase, Room, Mock for testing).
 */
interface RouteRepository {

    /**
     * Fetches all active routes from the data source.
     * @return Result containing list of routes or an error.
     */
    suspend fun getAllRoutes(): Result<List<Route>>

    /**
     * Fetches a single route by its ID.
     * @param routeId The unique identifier of the route.
     * @return Result containing the route or an error.
     */
    suspend fun getRouteById(routeId: String): Result<Route>

    /**
     * Fetches all stops for a specific route.
     * @param routeId The unique identifier of the route.
     * @return Result containing list of stops ordered by sequence.
     */
    suspend fun getStopsByRouteId(routeId: String): Result<List<Stop>>

    /**
     * Fetches all stops from all routes.
     * @return Result containing list of all stops.
     */
    suspend fun getAllStops(): Result<List<Stop>>

    /**
     * Fetches a complete route with all its stops.
     * @param routeId The unique identifier of the route.
     * @return Result containing the route with its stops.
     */
    suspend fun getCompleteRoute(routeId: String): Result<Pair<Route, List<Stop>>>
}
