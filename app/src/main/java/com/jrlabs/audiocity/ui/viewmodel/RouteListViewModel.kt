package com.jrlabs.audiocity.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.audiocity.domain.common.AudioCityError
import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.RouteCategory
import com.jrlabs.audiocity.domain.model.RouteSection
import com.jrlabs.audiocity.domain.model.Trip
import com.jrlabs.audiocity.domain.usecase.favorite.ToggleFavoriteUseCase
import com.jrlabs.audiocity.domain.usecase.route.GetAllRoutesUseCase
import com.jrlabs.audiocity.domain.usecase.trip.GetAllTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Routes List screen.
 *
 * Follows SOLID principles:
 * - S: Single Responsibility - only manages routes list UI state
 * - O: Open/Closed - uses interfaces, extensible without modification
 * - L: Liskov - uses abstract types (interfaces)
 * - I: Interface Segregation - depends on specific use cases
 * - D: Dependency Inversion - depends on abstractions via Hilt
 *
 * Uses Use Cases to encapsulate business logic,
 * making the ViewModel thin and focused on UI state management.
 */
@HiltViewModel
class RouteListViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<RouteListUiState>(RouteListUiState.Loading)
    val uiState: StateFlow<RouteListUiState> = _uiState.asStateFlow()

    // Routes data
    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    // Trips data
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    // Favorites - combine with routes for reactive updates
    val favoriteRoutes: StateFlow<List<Route>> = combine(
        _routes,
        toggleFavoriteUseCase.favoriteRouteIds
    ) { routes, favoriteIds ->
        routes.filter { favoriteIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Route sections for UI
    val routeSections: StateFlow<List<RouteSection>> = _routes.combine(
        toggleFavoriteUseCase.favoriteRouteIds
    ) { routes, _ ->
        buildRouteSections(routes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    /**
     * Loads all data needed for the screen.
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = RouteListUiState.Loading

            // Load routes and trips in parallel
            val routesResult = getAllRoutesUseCase()
            val tripsResult = getAllTripsUseCase()

            when (routesResult) {
                is Result.Success -> {
                    _routes.value = routesResult.data
                    _uiState.value = RouteListUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = RouteListUiState.Error(routesResult.error)
                }
            }

            // Trips are optional, don't fail the whole screen if they fail
            if (tripsResult is Result.Success) {
                _trips.value = tripsResult.data
            }
        }
    }

    /**
     * Toggles favorite status for a route.
     */
    fun toggleFavorite(routeId: String) {
        toggleFavoriteUseCase(routeId)
    }

    /**
     * Checks if a route is favorited.
     */
    fun isFavorite(routeId: String): Boolean {
        return toggleFavoriteUseCase.isFavorite(routeId)
    }

    /**
     * Builds route sections for the UI.
     */
    private fun buildRouteSections(routes: List<Route>): List<RouteSection> {
        val sections = mutableListOf<RouteSection>()

        // Top Routes - sorted by number of stops
        val topRoutes = routes.sortedByDescending { it.numStops }.take(5)
        if (topRoutes.isNotEmpty()) {
            sections.add(
                RouteSection(
                    id = "top",
                    category = RouteCategory.TOP,
                    routes = topRoutes
                )
            )
        }

        // Trending Routes (mock - same as iOS)
        val trendingRoutes = routes.shuffled().take(4)
        if (trendingRoutes.isNotEmpty()) {
            sections.add(
                RouteSection(
                    id = "trending",
                    category = RouteCategory.TRENDING,
                    routes = trendingRoutes
                )
            )
        }

        return sections
    }
}

/**
 * Sealed class representing the UI state of the Routes List screen.
 * Follows Open/Closed Principle - new states can be added without modifying existing code.
 */
sealed class RouteListUiState {
    data object Loading : RouteListUiState()
    data object Success : RouteListUiState()
    data class Error(val error: AudioCityError) : RouteListUiState()
}
