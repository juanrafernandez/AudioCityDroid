package com.jrlabs.audiocity.domain.service

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing route favorites.
 * Follows Single Responsibility Principle - only handles favorites.
 */
interface FavoritesService {

    /**
     * Flow of favorite route IDs.
     */
    val favoriteRouteIds: StateFlow<Set<String>>

    /**
     * Toggles the favorite status of a route.
     * @param routeId The ID of the route to toggle.
     * @return true if the route is now a favorite, false if removed.
     */
    fun toggleFavorite(routeId: String): Boolean

    /**
     * Checks if a route is marked as favorite.
     * @param routeId The ID of the route to check.
     */
    fun isFavorite(routeId: String): Boolean

    /**
     * Adds a route to favorites.
     * @param routeId The ID of the route to add.
     */
    fun addFavorite(routeId: String)

    /**
     * Removes a route from favorites.
     * @param routeId The ID of the route to remove.
     */
    fun removeFavorite(routeId: String)

    /**
     * Clears all favorites.
     */
    fun clearAll()
}
