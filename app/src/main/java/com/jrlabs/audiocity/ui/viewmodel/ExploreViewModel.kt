package com.jrlabs.audiocity.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.data.repository.FirebaseRepository
import com.jrlabs.audiocity.services.AudioService
import com.jrlabs.audiocity.services.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val locationService: LocationService,
    private val audioService: AudioService
) : ViewModel() {

    private val _allStops = MutableStateFlow<List<Stop>>(emptyList())
    val allStops: StateFlow<List<Stop>> = _allStops.asStateFlow()

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedStop = MutableStateFlow<Stop?>(null)
    val selectedStop: StateFlow<Stop?> = _selectedStop.asStateFlow()

    val currentLocation: StateFlow<Location?> = locationService.currentLocation
    val isPlaying: StateFlow<Boolean> = audioService.isPlaying
    val isPaused: StateFlow<Boolean> = audioService.isPaused

    init {
        loadAllStops()
    }

    fun loadAllStops() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Load routes first
            val routesResult = firebaseRepository.fetchAllRoutes()
            routesResult.fold(
                onSuccess = { routes ->
                    _routes.value = routes

                    // Then load all stops
                    val stopsResult = firebaseRepository.fetchAllStops()
                    stopsResult.fold(
                        onSuccess = { stops ->
                            _allStops.value = stops
                        },
                        onFailure = { error ->
                            _errorMessage.value = error.message
                        }
                    )
                },
                onFailure = { error ->
                    _errorMessage.value = error.message
                }
            )

            _isLoading.value = false
        }
    }

    fun selectStop(stop: Stop?) {
        _selectedStop.value = stop
    }

    fun playStop(stop: Stop) {
        audioService.clearQueue()
        audioService.enqueueStop(stop)
    }

    fun stopAudio() {
        audioService.stop()
    }

    fun pauseAudio() {
        audioService.pause()
    }

    fun resumeAudio() {
        audioService.resume()
    }

    fun startLocationTracking() {
        locationService.startTracking()
    }

    fun stopLocationTracking() {
        locationService.stopTracking()
    }

    fun getNearbyStops(limit: Int = 10): List<Stop> {
        val location = locationService.currentLocation.value ?: return emptyList()

        return _allStops.value
            .map { stop ->
                val distance = locationService.distanceBetween(
                    location.latitude,
                    location.longitude,
                    stop.latitude,
                    stop.longitude
                )
                stop to distance
            }
            .filter { it.second <= 500 } // Within 500 meters
            .sortedBy { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getRouteForStop(stop: Stop): Route? {
        return _routes.value.find { it.id == stop.routeId }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
