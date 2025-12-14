package com.jrlabs.audiocity.data.service

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.domain.service.FavoritesService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local SharedPreferences implementation of FavoritesService.
 * Follows Single Responsibility Principle - only handles favorites persistence.
 */
@Singleton
class LocalFavoritesService @Inject constructor(
    @ApplicationContext private val context: Context
) : FavoritesService {

    companion object {
        private const val PREFS_NAME = "audiocity_favorites"
        private const val KEY_FAVORITES = "favorite_route_ids"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteRouteIds: StateFlow<Set<String>> = _favoriteRouteIds.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val stored = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        _favoriteRouteIds.value = stored.toSet()
    }

    private fun saveFavorites() {
        prefs.edit()
            .putStringSet(KEY_FAVORITES, _favoriteRouteIds.value)
            .apply()
    }

    override fun toggleFavorite(routeId: String): Boolean {
        val currentFavorites = _favoriteRouteIds.value.toMutableSet()
        val isNowFavorite = if (currentFavorites.contains(routeId)) {
            currentFavorites.remove(routeId)
            false
        } else {
            currentFavorites.add(routeId)
            true
        }
        _favoriteRouteIds.value = currentFavorites.toSet()
        saveFavorites()
        return isNowFavorite
    }

    override fun isFavorite(routeId: String): Boolean {
        return _favoriteRouteIds.value.contains(routeId)
    }

    override fun addFavorite(routeId: String) {
        if (!isFavorite(routeId)) {
            val currentFavorites = _favoriteRouteIds.value.toMutableSet()
            currentFavorites.add(routeId)
            _favoriteRouteIds.value = currentFavorites.toSet()
            saveFavorites()
        }
    }

    override fun removeFavorite(routeId: String) {
        if (isFavorite(routeId)) {
            val currentFavorites = _favoriteRouteIds.value.toMutableSet()
            currentFavorites.remove(routeId)
            _favoriteRouteIds.value = currentFavorites.toSet()
            saveFavorites()
        }
    }

    override fun clearAll() {
        _favoriteRouteIds.value = emptySet()
        saveFavorites()
    }
}
