package com.jrlabs.audiocity.domain.repository

import com.jrlabs.audiocity.domain.model.Destination
import com.jrlabs.audiocity.domain.model.Trip
import com.jrlabs.audiocity.domain.common.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Trip operations.
 * Handles both local persistence and remote synchronization.
 */
interface TripRepository {

    /**
     * Observable flow of all trips.
     * Emits whenever the trips list changes.
     */
    val trips: Flow<List<Trip>>

    /**
     * Gets all trips.
     */
    suspend fun getAllTrips(): Result<List<Trip>>

    /**
     * Gets a trip by its ID.
     */
    suspend fun getTripById(tripId: String): Result<Trip>

    /**
     * Saves a new trip or updates an existing one.
     */
    suspend fun saveTrip(trip: Trip): Result<Unit>

    /**
     * Deletes a trip by its ID.
     */
    suspend fun deleteTrip(tripId: String): Result<Unit>

    /**
     * Gets all available destinations.
     */
    suspend fun getDestinations(): Result<List<Destination>>
}
