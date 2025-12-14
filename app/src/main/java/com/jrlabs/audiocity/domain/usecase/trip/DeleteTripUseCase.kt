package com.jrlabs.audiocity.domain.usecase.trip

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for deleting a trip.
 */
class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Executes the use case.
     * @param tripId The ID of the trip to delete.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(tripId: String): Result<Unit> {
        return tripRepository.deleteTrip(tripId)
    }
}
