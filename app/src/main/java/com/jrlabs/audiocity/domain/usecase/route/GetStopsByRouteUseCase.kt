package com.jrlabs.audiocity.domain.usecase.route

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case for fetching all stops of a route.
 */
class GetStopsByRouteUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {

    /**
     * Executes the use case.
     * @param routeId The unique identifier of the route.
     * @return Result containing list of stops ordered by sequence.
     */
    suspend operator fun invoke(routeId: String): Result<List<Stop>> {
        return routeRepository.getStopsByRouteId(routeId)
    }
}
