package com.jrlabs.audiocity.domain.usecase.trip

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Trip
import com.jrlabs.audiocity.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching all trips.
 */
class GetAllTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Executes the use case.
     * @return Result containing list of all trips.
     */
    suspend operator fun invoke(): Result<List<Trip>> {
        return tripRepository.getAllTrips()
    }

    /**
     * Observes trips as a Flow for reactive updates.
     */
    fun observe(): Flow<List<Trip>> {
        return tripRepository.trips
    }
}
