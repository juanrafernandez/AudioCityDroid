package com.jrlabs.audiocity.domain.usecase.route

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case for fetching all stops across all routes.
 * Useful for the explore/map screen.
 */
class GetAllStopsUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {

    /**
     * Executes the use case.
     * @return Result containing list of all stops.
     */
    suspend operator fun invoke(): Result<List<Stop>> {
        return routeRepository.getAllStops()
    }
}
