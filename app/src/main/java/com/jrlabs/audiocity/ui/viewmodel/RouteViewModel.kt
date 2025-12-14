package com.jrlabs.audiocity.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.audiocity.data.model.AudioQueueItem
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.data.repository.FirebaseRepository
import com.jrlabs.audiocity.domain.model.RouteHistoryEntry
import com.jrlabs.audiocity.domain.service.HistoryService
import com.jrlabs.audiocity.services.AudioService
import com.jrlabs.audiocity.services.FavoritesService
import com.jrlabs.audiocity.services.GeofenceService
import com.jrlabs.audiocity.services.LocationForegroundService
import com.jrlabs.audiocity.services.LocationService
import com.jrlabs.audiocity.services.RoutingService
import com.jrlabs.audiocity.services.TripService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    application: Application,
    private val firebaseRepository: FirebaseRepository,
    val locationService: LocationService,
    private val geofenceService: GeofenceService,
    val audioService: AudioService,
    private val tripService: TripService,
    private val favoritesService: FavoritesService,
    private val routingService: RoutingService,
    private val historyService: HistoryService
) : AndroidViewModel(application) {

    // Routes
    private val _availableRoutes = MutableStateFlow<List<Route>>(emptyList())
    val availableRoutes: StateFlow<List<Route>> = _availableRoutes.asStateFlow()

    private val _currentRoute = MutableStateFlow<Route?>(null)
    val currentRoute: StateFlow<Route?> = _currentRoute.asStateFlow()

    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()

    // Route State
    private val _isRouteActive = MutableStateFlow(false)
    val isRouteActive: StateFlow<Boolean> = _isRouteActive.asStateFlow()

    private val _currentStop = MutableStateFlow<Stop?>(null)
    val currentStop: StateFlow<Stop?> = _currentStop.asStateFlow()

    // Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingRoutes = MutableStateFlow(false)
    val isLoadingRoutes: StateFlow<Boolean> = _isLoadingRoutes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Progress
    private val _visitedStopsCount = MutableStateFlow(0)
    val visitedStopsCount: StateFlow<Int> = _visitedStopsCount.asStateFlow()

    // Audio state from service
    val isPlaying: StateFlow<Boolean> = audioService.isPlaying
    val isPaused: StateFlow<Boolean> = audioService.isPaused
    val currentQueueItem: StateFlow<AudioQueueItem?> = audioService.currentQueueItem
    val queuedItems: StateFlow<List<AudioQueueItem>> = audioService.queuedItems

    // Trips and Favorites
    val trips: StateFlow<List<Trip>> = tripService.trips
    val favoriteRouteIds: StateFlow<Set<String>> = favoritesService.favoriteRouteIds

    // History
    val historyEntries: StateFlow<List<RouteHistoryEntry>> = historyService.historyEntries

    // User location for sorting routes by proximity
    data class UserLocationData(val latitude: Double, val longitude: Double)
    private val _userLocation = MutableStateFlow<UserLocationData?>(null)
    val userLocation: StateFlow<UserLocationData?> = _userLocation.asStateFlow()

    // Walking route path (calculated by OSRM)
    private val _walkingRoutePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val walkingRoutePoints: StateFlow<List<LatLng>> = _walkingRoutePoints.asStateFlow()

    // Route calculation state
    private val _isCalculatingRoute = MutableStateFlow(false)
    val isCalculatingRoute: StateFlow<Boolean> = _isCalculatingRoute.asStateFlow()

    init {
        observeTriggeredStops()
        observeLocationUpdates()
        observeCurrentQueueItem()
    }

    private fun observeTriggeredStops() {
        viewModelScope.launch {
            geofenceService.triggeredStop.collectLatest { stop ->
                handleStopTriggered(stop)
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationService.currentLocation.collectLatest { location ->
                if (location != null) {
                    // Update user location for proximity sorting
                    _userLocation.value = UserLocationData(location.latitude, location.longitude)

                    if (_isRouteActive.value) {
                        geofenceService.checkProximity()
                        _visitedStopsCount.value = geofenceService.getVisitedCount()
                    }
                }
            }
        }
    }

    // Request single location update for sorting
    fun requestLocationForSorting() {
        locationService.requestSingleLocation()
    }

    private fun observeCurrentQueueItem() {
        viewModelScope.launch {
            audioService.currentQueueItem.collectLatest { queueItem ->
                if (queueItem != null) {
                    val stop = _stops.value.find { it.id == queueItem.stopId }
                    _currentStop.value = stop
                } else if (!audioService.isPlaying.value) {
                    _currentStop.value = null
                }
            }
        }
    }

    private fun handleStopTriggered(stop: Stop) {
        // Update local stops list
        _stops.value = _stops.value.map {
            if (it.id == stop.id) it.copy(hasBeenVisited = true) else it
        }
        _visitedStopsCount.value = geofenceService.getVisitedCount()

        // Enqueue audio
        audioService.enqueueStop(stop)
    }

    // Load all available routes
    fun loadAvailableRoutes() {
        viewModelScope.launch {
            _isLoadingRoutes.value = true
            _errorMessage.value = null

            val result = firebaseRepository.fetchAllRoutes()
            result.fold(
                onSuccess = { routes ->
                    _availableRoutes.value = routes
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )

            _isLoadingRoutes.value = false
        }
    }

    // Select and load a specific route
    fun selectRoute(route: Route) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentRoute.value = route

            val result = firebaseRepository.fetchStops(route.id)
            result.fold(
                onSuccess = { stops ->
                    _stops.value = stops
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )

            _isLoading.value = false
        }
    }

    // Check if route optimization should be suggested
    fun shouldSuggestRouteOptimization(): Boolean {
        val location = _userLocation.value ?: return false
        val stops = _stops.value
        if (stops.isEmpty()) return false

        // Find the nearest stop
        val userLat = location.latitude
        val userLng = location.longitude

        val stopsWithDistance = stops.map { stop ->
            val distance = calculateDistance(userLat, userLng, stop.latitude, stop.longitude)
            stop to distance
        }

        val nearestStop = stopsWithDistance.minByOrNull { it.second }?.first
        val firstStop = stops.minByOrNull { it.order }

        // Suggest optimization if nearest stop is not the first one
        return nearestStop != null && firstStop != null && nearestStop.id != firstStop.id
    }

    // Get nearest stop info for dialog
    fun getNearestStopInfo(): NearestStopInfo? {
        val location = _userLocation.value ?: return null
        val stops = _stops.value
        if (stops.isEmpty()) return null

        val userLat = location.latitude
        val userLng = location.longitude

        val stopsWithDistance = stops.map { stop ->
            val distance = calculateDistance(userLat, userLng, stop.latitude, stop.longitude)
            stop to distance
        }

        val nearest = stopsWithDistance.minByOrNull { it.second } ?: return null
        return NearestStopInfo(
            name = nearest.first.name,
            distanceMeters = nearest.second.toInt(),
            originalOrder = nearest.first.order
        )
    }

    data class NearestStopInfo(
        val name: String,
        val distanceMeters: Int,
        val originalOrder: Int
    )

    // Optimize route starting from nearest stop
    private fun optimizeRouteFromCurrentLocation() {
        val location = _userLocation.value ?: return
        val stops = _stops.value.toMutableList()
        if (stops.isEmpty()) return

        val userLat = location.latitude
        val userLng = location.longitude

        // Find nearest stop
        val nearestIndex = stops.indices.minByOrNull { index ->
            calculateDistance(userLat, userLng, stops[index].latitude, stops[index].longitude)
        } ?: return

        // Reorder stops using nearest neighbor algorithm
        val optimizedStops = mutableListOf<Stop>()
        val remaining = stops.toMutableList()

        // Start from nearest
        var currentStop = remaining.removeAt(nearestIndex)
        optimizedStops.add(currentStop.copy(order = 1))

        // Add remaining stops by nearest neighbor
        var order = 2
        while (remaining.isNotEmpty()) {
            val nextIndex = remaining.indices.minByOrNull { index ->
                calculateDistance(
                    currentStop.latitude, currentStop.longitude,
                    remaining[index].latitude, remaining[index].longitude
                )
            } ?: break

            currentStop = remaining.removeAt(nextIndex)
            optimizedStops.add(currentStop.copy(order = order++))
        }

        _stops.value = optimizedStops
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Calculate walking route and start when ready
    fun prepareAndStartRoute(optimized: Boolean = false, onRouteReady: () -> Unit) {
        val route = _currentRoute.value ?: return
        val stops = _stops.value
        if (stops.isEmpty()) return

        _isCalculatingRoute.value = true

        // Optimize if requested
        if (optimized) {
            optimizeRouteFromCurrentLocation()
        }

        // Calculate walking route using OSRM
        viewModelScope.launch {
            val sortedStops = _stops.value.sortedBy { it.order }
            val stopLocations = sortedStops.map { LatLng(it.latitude, it.longitude) }

            // Get user's current location as starting point
            val userLocation = _userLocation.value
            val userLatLng = if (userLocation != null) {
                LatLng(userLocation.latitude, userLocation.longitude)
            } else {
                // If no user location, use first stop as starting point
                stopLocations.firstOrNull()
            }

            val result = if (userLatLng != null) {
                // Calculate complete route: User -> Stop1 -> Stop2 -> ... -> StopN
                Log.d("RouteViewModel", "Calculating route from user location through ${stopLocations.size} stops")
                routingService.getCompleteWalkingRoute(userLatLng, stopLocations)
            } else {
                Result.failure(Exception("No location available"))
            }

            result.fold(
                onSuccess = { points ->
                    _walkingRoutePoints.value = points
                    Log.d("RouteViewModel", "Walking route calculated with ${points.size} points")
                },
                onFailure = { error ->
                    Log.e("RouteViewModel", "Failed to calculate route: ${error.message}")
                    // Fallback to straight lines between stops (including user location if available)
                    val fallbackPoints = if (userLatLng != null) {
                        listOf(userLatLng) + stopLocations
                    } else {
                        stopLocations
                    }
                    _walkingRoutePoints.value = fallbackPoints
                }
            )

            // Now start the route
            _isRouteActive.value = true
            _visitedStopsCount.value = 0

            // Start location tracking
            locationService.startTracking()

            // Setup geofences with potentially optimized stops
            geofenceService.setupGeofences(_stops.value)

            // Start foreground service for background tracking
            startForegroundService(route.name)

            // Record route start in history
            historyService.recordRouteStarted(
                routeId = route.id,
                routeName = route.name,
                routeCity = route.city,
                totalStops = _stops.value.size,
                distanceKm = route.distanceKm
            )

            _isCalculatingRoute.value = false

            // Notify that route is ready
            onRouteReady()
        }
    }

    // Legacy method for compatibility
    fun startRoute(optimized: Boolean = false) {
        prepareAndStartRoute(optimized) { }
    }

    // End the route
    fun endRoute() {
        // Check if route was completed (all stops visited)
        val totalStops = _stops.value.size
        val visitedStops = _visitedStopsCount.value
        val wasCompleted = visitedStops >= totalStops && totalStops > 0

        // Record route end in history
        if (historyService.hasActiveRoute()) {
            historyService.recordRouteEnded(
                stopsVisited = visitedStops,
                wasCompleted = wasCompleted
            )
        }

        _isRouteActive.value = false

        // Stop location tracking
        locationService.stopTracking()

        // Clear geofences
        geofenceService.clearGeofences()

        // Stop audio
        audioService.clearQueue()

        // Stop foreground service
        stopForegroundService()

        // Reset state
        _currentStop.value = null
        _visitedStopsCount.value = 0
        _walkingRoutePoints.value = emptyList()
    }

    fun backToRoutesList() {
        _currentRoute.value = null
        _stops.value = emptyList()
    }

    // Audio controls
    fun pauseAudio() = audioService.pause()
    fun resumeAudio() = audioService.resume()
    fun stopAudio() = audioService.stop()
    fun skipToNext() = audioService.skipToNext()

    // Play a specific stop manually
    fun playStop(stop: Stop) {
        audioService.enqueueStop(stop)
        geofenceService.markStopAsVisited(stop.id)
        _stops.value = _stops.value.map {
            if (it.id == stop.id) it.copy(hasBeenVisited = true) else it
        }
        _visitedStopsCount.value = geofenceService.getVisitedCount()
    }

    // Progress
    fun getProgress(): Float = geofenceService.getProgress()

    fun clearError() {
        _errorMessage.value = null
    }

    // Favorites
    fun toggleFavorite(routeId: String) {
        favoritesService.toggleFavorite(routeId)
    }

    fun isFavorite(routeId: String): Boolean {
        return favoritesService.isFavorite(routeId)
    }

    // Trips
    fun getTrip(tripId: String): Trip? {
        return tripService.getTrip(tripId)
    }

    // History
    val groupedHistory = historyService.groupedHistory
    fun clearHistory() = historyService.clearHistory()

    private fun startForegroundService(routeName: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            putExtra(LocationForegroundService.EXTRA_ROUTE_NAME, routeName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService() {
        val context = getApplication<Application>()
        val intent = Intent(context, LocationForegroundService::class.java)
        context.stopService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRouteActive.value) {
            endRoute()
        }
    }
}
