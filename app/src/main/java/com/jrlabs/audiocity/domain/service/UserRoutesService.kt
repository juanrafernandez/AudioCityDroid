package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.UserRoute
import com.jrlabs.audiocity.domain.model.UserStop
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for user-created routes service (UGC).
 * Handles CRUD operations for routes created by users.
 */
interface UserRoutesService {

    /**
     * All routes created by the user.
     */
    val userRoutes: StateFlow<List<UserRoute>>

    /**
     * Published routes (shared with community).
     */
    val publishedRoutes: StateFlow<List<UserRoute>>

    /**
     * Creates a new route.
     * @param route The route to create.
     * @return The created route with ID.
     */
    fun createRoute(route: UserRoute): UserRoute

    /**
     * Updates an existing route.
     * @param route The updated route.
     */
    fun updateRoute(route: UserRoute)

    /**
     * Deletes a route.
     * @param routeId The ID of the route to delete.
     */
    fun deleteRoute(routeId: String)

    /**
     * Gets a route by ID.
     * @param routeId The ID of the route.
     */
    fun getRouteById(routeId: String): UserRoute?

    /**
     * Adds a stop to a route.
     * @param routeId The ID of the route.
     * @param stop The stop to add.
     */
    fun addStopToRoute(routeId: String, stop: UserStop)

    /**
     * Removes a stop from a route.
     * @param routeId The ID of the route.
     * @param stopId The ID of the stop to remove.
     */
    fun removeStopFromRoute(routeId: String, stopId: String)

    /**
     * Reorders stops in a route.
     * @param routeId The ID of the route.
     * @param stopIds The new order of stop IDs.
     */
    fun reorderStops(routeId: String, stopIds: List<String>)

    /**
     * Publishes a route (makes it available to others).
     * @param routeId The ID of the route to publish.
     */
    fun publishRoute(routeId: String)

    /**
     * Unpublishes a route.
     * @param routeId The ID of the route to unpublish.
     */
    fun unpublishRoute(routeId: String)
}
