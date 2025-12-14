package com.jrlabs.audiocity.domain.usecase.route

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case for fetching a single route by its ID.
 */
class GetRouteByIdUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {

    /**
     * Executes the use case.
     * @param routeId The unique identifier of the route.
     * @return Result containing the route.
     */
    suspend operator fun invoke(routeId: String): Result<Route> {
        return routeRepository.getRouteById(routeId)
    }
}
