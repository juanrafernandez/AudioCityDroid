package com.jrlabs.audiocity.domain.usecase.route

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case for fetching a complete route with all its stops.
 */
class GetCompleteRouteUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {

    /**
     * Executes the use case.
     * @param routeId The unique identifier of the route.
     * @return Result containing the route and its ordered stops.
     */
    suspend operator fun invoke(routeId: String): Result<Pair<Route, List<Stop>>> {
        return routeRepository.getCompleteRoute(routeId)
    }
}
