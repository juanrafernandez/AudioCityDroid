package com.jrlabs.audiocity.domain.usecase.route

import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case for fetching all available routes.
 *
 * Follows Single Responsibility Principle - one action per use case.
 * Follows Dependency Inversion Principle - depends on abstraction (RouteRepository).
 */
class GetAllRoutesUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {

    /**
     * Executes the use case.
     * @return Result containing list of all active routes.
     */
    suspend operator fun invoke(): Result<List<Route>> {
        return routeRepository.getAllRoutes()
    }
}
