package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.Stop
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for geofencing service.
 * Handles proximity detection for stops/points of interest.
 *
 * Follows Interface Segregation Principle - only geofence-related operations.
 */
interface GeofenceService {

    /**
     * Flow of the currently triggered stop.
     */
    val triggeredStop: SharedFlow<Stop>

    /**
     * Flow of visited stop IDs.
     */
    val visitedStopIds: StateFlow<Set<String>>

    /**
     * Flow indicating the current progress (visited stops count).
     */
    val progress: StateFlow<Int>

    /**
     * Flow of the total number of stops being monitored.
     */
    val totalStops: StateFlow<Int>

    /**
     * Sets up geofences for a list of stops.
     * @param stops The stops to monitor.
     */
    suspend fun setupGeofences(stops: List<Stop>)

    /**
     * Removes all active geofences.
     */
    fun removeAllGeofences()

    /**
     * Triggers a stop manually (e.g., when user enters geofence).
     * @param stopId The ID of the triggered stop.
     */
    suspend fun triggerStop(stopId: String)

    /**
     * Marks a stop as visited.
     * @param stopId The ID of the stop to mark.
     */
    fun markStopAsVisited(stopId: String)

    /**
     * Resets all visited stops and progress.
     */
    fun reset()

    /**
     * Checks if a stop has been visited.
     * @param stopId The ID of the stop to check.
     */
    fun isStopVisited(stopId: String): Boolean
}
