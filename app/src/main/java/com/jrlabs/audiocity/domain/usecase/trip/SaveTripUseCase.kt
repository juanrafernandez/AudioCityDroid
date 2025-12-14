package com.jrlabs.audiocity.domain.usecase.trip

import com.jrlabs.audiocity.domain.common.AudioCityError
import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Trip
import com.jrlabs.audiocity.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for saving a trip.
 * Contains validation logic.
 */
class SaveTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Executes the use case with validation.
     * @param trip The trip to save.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(trip: Trip): Result<Unit> {
        // Validate trip data
        if (trip.destinationCity.isBlank()) {
            return Result.error(
                AudioCityError.Data.ValidationError(
                    field = "destinationCity",
                    message = "Destination city is required"
                )
            )
        }

        if (trip.selectedRouteIds.isEmpty()) {
            return Result.error(
                AudioCityError.Data.ValidationError(
                    field = "selectedRouteIds",
                    message = "At least one route must be selected"
                )
            )
        }

        // Validate date range if provided
        if (trip.startDate != null && trip.endDate != null) {
            if (trip.endDate.before(trip.startDate)) {
                return Result.error(AudioCityError.Trip.InvalidDateRange())
            }
        }

        return tripRepository.saveTrip(trip)
    }
}
