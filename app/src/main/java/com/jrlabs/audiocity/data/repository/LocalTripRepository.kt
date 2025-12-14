package com.jrlabs.audiocity.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.data.dto.TripDto
import com.jrlabs.audiocity.data.mapper.TripMapper
import com.jrlabs.audiocity.domain.common.AudioCityError
import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Destination
import com.jrlabs.audiocity.domain.model.Trip
import com.jrlabs.audiocity.domain.repository.RouteRepository
import com.jrlabs.audiocity.domain.repository.TripRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local implementation of TripRepository using SharedPreferences.
 * Follows Repository pattern for data access abstraction.
 */
@Singleton
class LocalTripRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripMapper: TripMapper,
    private val routeRepository: RouteRepository
) : TripRepository {

    companion object {
        private const val PREFS_NAME = "audiocity_trips"
        private const val KEY_TRIPS = "trips"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    override val trips: Flow<List<Trip>> = _trips.asStateFlow()

    init {
        loadTripsFromPrefs()
    }

    private fun loadTripsFromPrefs() {
        try {
            val tripsJson = prefs.getString(KEY_TRIPS, null)
            if (tripsJson != null) {
                val dtos = json.decodeFromString<List<TripDto>>(tripsJson)
                _trips.value = tripMapper.toDomainList(dtos)
            }
        } catch (e: Exception) {
            _trips.value = emptyList()
        }
    }

    private fun saveTripsToPrefs(trips: List<Trip>) {
        try {
            val dtos = tripMapper.toDtoList(trips)
            val tripsJson = json.encodeToString(dtos)
            prefs.edit().putString(KEY_TRIPS, tripsJson).apply()
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    override suspend fun getAllTrips(): Result<List<Trip>> {
        return Result.success(_trips.value)
    }

    override suspend fun getTripById(tripId: String): Result<Trip> {
        val trip = _trips.value.find { it.id == tripId }
        return if (trip != null) {
            Result.success(trip)
        } else {
            Result.error(AudioCityError.Trip.NotFound(tripId))
        }
    }

    override suspend fun saveTrip(trip: Trip): Result<Unit> {
        return try {
            val currentTrips = _trips.value.toMutableList()
            val existingIndex = currentTrips.indexOfFirst { it.id == trip.id }

            if (existingIndex >= 0) {
                currentTrips[existingIndex] = trip
            } else {
                currentTrips.add(trip)
            }

            _trips.value = currentTrips.toList()
            saveTripsToPrefs(_trips.value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Trip.SaveFailed(
                    message = "Failed to save trip: ${e.message}",
                    cause = e
                )
            )
        }
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            val currentTrips = _trips.value.filter { it.id != tripId }
            _trips.value = currentTrips
            saveTripsToPrefs(_trips.value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Trip.SaveFailed(
                    message = "Failed to delete trip: ${e.message}",
                    cause = e
                )
            )
        }
    }

    override suspend fun getDestinations(): Result<List<Destination>> {
        // Get destinations from available routes
        return when (val routesResult = routeRepository.getAllRoutes()) {
            is Result.Success -> {
                val destinations = routesResult.data
                    .groupBy { it.city }
                    .map { (city, routes) ->
                        Destination(
                            id = city.lowercase().replace(" ", "_"),
                            city = city,
                            country = "España",
                            routeCount = routes.size,
                            isPopular = routes.size >= 3
                        )
                    }
                    .sortedByDescending { it.routeCount }

                Result.success(destinations)
            }
            is Result.Error -> routesResult
        }
    }
}
