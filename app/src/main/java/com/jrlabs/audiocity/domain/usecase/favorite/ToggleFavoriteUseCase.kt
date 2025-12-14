package com.jrlabs.audiocity.domain.usecase.favorite

import com.jrlabs.audiocity.domain.service.FavoritesService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for toggling route favorites.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesService: FavoritesService
) {

    /**
     * Observable flow of favorite route IDs.
     */
    val favoriteRouteIds: StateFlow<Set<String>>
        get() = favoritesService.favoriteRouteIds

    /**
     * Toggles the favorite status of a route.
     * @param routeId The ID of the route to toggle.
     * @return true if the route is now a favorite, false otherwise.
     */
    operator fun invoke(routeId: String): Boolean {
        return favoritesService.toggleFavorite(routeId)
    }

    /**
     * Checks if a route is a favorite.
     */
    fun isFavorite(routeId: String): Boolean {
        return favoritesService.isFavorite(routeId)
    }
}
