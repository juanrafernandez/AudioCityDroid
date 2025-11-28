package com.jrlabs.audiocity.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.audiocity.data.model.AudioQueueItem
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.data.repository.FirebaseRepository
import com.jrlabs.audiocity.services.AudioService
import com.jrlabs.audiocity.services.GeofenceService
import com.jrlabs.audiocity.services.LocationForegroundService
import com.jrlabs.audiocity.services.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    application: Application,
    private val firebaseRepository: FirebaseRepository,
    val locationService: LocationService,
    private val geofenceService: GeofenceService,
    val audioService: AudioService
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
                if (location != null && _isRouteActive.value) {
                    geofenceService.checkProximity()
                    _visitedStopsCount.value = geofenceService.getVisitedCount()
                }
            }
        }
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

    // Start the route
    fun startRoute() {
        val route = _currentRoute.value ?: return
        val stops = _stops.value
        if (stops.isEmpty()) return

        _isRouteActive.value = true
        _visitedStopsCount.value = 0

        // Start location tracking
        locationService.startTracking()

        // Setup geofences
        geofenceService.setupGeofences(stops)

        // Start foreground service for background tracking
        startForegroundService(route.name)
    }

    // End the route
    fun endRoute() {
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
