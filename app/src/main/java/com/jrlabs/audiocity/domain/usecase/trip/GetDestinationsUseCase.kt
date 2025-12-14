package com.jrlabs.audiocity.domain.usecase.trip

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Destination
import com.jrlabs.audiocity.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for fetching available destinations.
 */
class GetDestinationsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Executes the use case.
     * @return Result containing list of available destinations.
     */
    suspend operator fun invoke(): Result<List<Destination>> {
        return tripRepository.getDestinations()
    }
}
