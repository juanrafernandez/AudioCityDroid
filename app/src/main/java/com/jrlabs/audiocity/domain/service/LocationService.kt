package com.jrlabs.audiocity.domain.service

import android.location.Location
import com.jrlabs.audiocity.domain.model.Stop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for location tracking service.
 * Follows Interface Segregation Principle - only location-related operations.
 *
 * Allows for different implementations:
 * - FusedLocationProvider (current)
 * - Mock for testing
 */
interface LocationService {

    /**
     * Flow of the current user location.
     */
    val currentLocation: StateFlow<Location?>

    /**
     * Flow indicating whether location tracking is active.
     */
    val isTracking: StateFlow<Boolean>

    /**
     * Checks if fine location permission is granted.
     */
    fun hasLocationPermission(): Boolean

    /**
     * Checks if background location permission is granted.
     */
    fun hasBackgroundLocationPermission(): Boolean

    /**
     * Starts tracking the user's location.
     */
    fun startTracking()

    /**
     * Stops tracking the user's location.
     */
    fun stopTracking()

    /**
     * Returns a Flow of location updates.
     */
    fun getLocationUpdates(): Flow<Location>

    /**
     * Calculates the distance from current location to a stop.
     * @param stop The stop to calculate distance to.
     * @return Distance in meters, or null if current location is unavailable.
     */
    fun distanceTo(stop: Stop): Float?

    /**
     * Calculates the distance between two coordinates.
     * @return Distance in meters.
     */
    fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float
}
