package com.jrlabs.audiocity.services

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.data.model.Route
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para gestión de rutas favoritas
 * Equivalente a FavoritesService.swift en iOS
 */
@Singleton
class FavoritesService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)

    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRouteIds: StateFlow<Set<String>> = _favoriteRouteIds.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Número de favoritos
     */
    val count: Int
        get() = _favoriteRouteIds.value.size

    /**
     * Verificar si una ruta es favorita
     */
    fun isFavorite(routeId: String): Boolean {
        return _favoriteRouteIds.value.contains(routeId)
    }

    /**
     * Añadir ruta a favoritos
     */
    fun addFavorite(routeId: String) {
        val updated = _favoriteRouteIds.value + routeId
        _favoriteRouteIds.value = updated
        saveFavorites()
    }

    /**
     * Quitar ruta de favoritos
     */
    fun removeFavorite(routeId: String) {
        val updated = _favoriteRouteIds.value - routeId
        _favoriteRouteIds.value = updated
        saveFavorites()
    }

    /**
     * Toggle favorito (añadir si no está, quitar si está)
     */
    fun toggleFavorite(routeId: String) {
        if (isFavorite(routeId)) {
            removeFavorite(routeId)
        } else {
            addFavorite(routeId)
        }
    }

    /**
     * Filtrar rutas favoritas de una lista
     */
    fun filterFavorites(routes: List<Route>): List<Route> {
        return routes.filter { _favoriteRouteIds.value.contains(it.id) }
    }

    // MARK: - Persistencia

    private fun loadFavorites() {
        val favoritesSet = prefs.getStringSet("favorite_route_ids", emptySet()) ?: emptySet()
        _favoriteRouteIds.value = favoritesSet
    }

    private fun saveFavorites() {
        prefs.edit()
            .putStringSet("favorite_route_ids", _favoriteRouteIds.value)
            .apply()
    }
}
