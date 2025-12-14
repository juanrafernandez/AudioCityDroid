package com.jrlabs.audiocity.domain.usecase.location

import android.location.Location
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.LocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for location tracking operations.
 */
class TrackLocationUseCase @Inject constructor(
    private val locationService: LocationService
) {

    val currentLocation: StateFlow<Location?>
        get() = locationService.currentLocation

    val isTracking: StateFlow<Boolean>
        get() = locationService.isTracking

    /**
     * Starts location tracking.
     */
    fun startTracking() {
        locationService.startTracking()
    }

    /**
     * Stops location tracking.
     */
    fun stopTracking() {
        locationService.stopTracking()
    }

    /**
     * Gets continuous location updates as a Flow.
     */
    fun getLocationUpdates(): Flow<Location> {
        return locationService.getLocationUpdates()
    }

    /**
     * Calculates distance to a stop.
     */
    fun distanceTo(stop: Stop): Float? {
        return locationService.distanceTo(stop)
    }

    /**
     * Checks if location permissions are granted.
     */
    fun hasPermission(): Boolean {
        return locationService.hasLocationPermission()
    }

    /**
     * Checks if background location permission is granted.
     */
    fun hasBackgroundPermission(): Boolean {
        return locationService.hasBackgroundLocationPermission()
    }
}
