package com.jrlabs.audiocity.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jrlabs.audiocity.data.dto.RouteDto
import com.jrlabs.audiocity.data.dto.StopDto
import com.jrlabs.audiocity.data.mapper.RouteMapper
import com.jrlabs.audiocity.data.mapper.StopMapper
import com.jrlabs.audiocity.domain.common.AudioCityError
import com.jrlabs.audiocity.domain.common.Result
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.repository.RouteRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of RouteRepository.
 * Follows Dependency Inversion Principle - implements the abstract RouteRepository interface.
 * Follows Single Responsibility Principle - only handles Firebase route operations.
 */
@Singleton
class FirebaseRouteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val routeMapper: RouteMapper,
    private val stopMapper: StopMapper
) : RouteRepository {

    companion object {
        private const val ROUTES_COLLECTION = "routes"
        private const val STOPS_COLLECTION = "stops"
    }

    override suspend fun getAllRoutes(): Result<List<Route>> {
        return try {
            val snapshot = firestore.collection(ROUTES_COLLECTION)
                .whereEqualTo("is_active", true)
                .get()
                .await()

            val dtos = snapshot.documents.mapNotNull { RouteDto.fromDocument(it) }
            val routes = routeMapper.toDomainList(dtos)
            Result.success(routes)
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Network.ServerError(
                    message = "Failed to fetch routes: ${e.message}",
                    code = 500,
                    cause = e
                )
            )
        }
    }

    override suspend fun getRouteById(routeId: String): Result<Route> {
        return try {
            val document = firestore.collection(ROUTES_COLLECTION)
                .document(routeId)
                .get()
                .await()

            val dto = RouteDto.fromDocument(document)
            if (dto != null) {
                Result.success(routeMapper.toDomain(dto))
            } else {
                Result.error(
                    AudioCityError.Data.NotFound(
                        entityType = "Route",
                        entityId = routeId
                    )
                )
            }
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Network.ServerError(
                    message = "Failed to fetch route: ${e.message}",
                    code = 500,
                    cause = e
                )
            )
        }
    }

    override suspend fun getStopsByRouteId(routeId: String): Result<List<Stop>> {
        return try {
            val snapshot = firestore.collection(STOPS_COLLECTION)
                .whereEqualTo("route_id", routeId)
                .get()
                .await()

            val dtos = snapshot.documents
                .mapNotNull { StopDto.fromDocument(it) }
                .sortedBy { it.order }

            val stops = stopMapper.toDomainList(dtos)
            Result.success(stops)
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Network.ServerError(
                    message = "Failed to fetch stops: ${e.message}",
                    code = 500,
                    cause = e
                )
            )
        }
    }

    override suspend fun getAllStops(): Result<List<Stop>> {
        return try {
            val snapshot = firestore.collection(STOPS_COLLECTION)
                .get()
                .await()

            val dtos = snapshot.documents.mapNotNull { StopDto.fromDocument(it) }
            val stops = stopMapper.toDomainList(dtos)
            Result.success(stops)
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Network.ServerError(
                    message = "Failed to fetch all stops: ${e.message}",
                    code = 500,
                    cause = e
                )
            )
        }
    }

    override suspend fun getCompleteRoute(routeId: String): Result<Pair<Route, List<Stop>>> {
        return try {
            val routeResult = getRouteById(routeId)
            val stopsResult = getStopsByRouteId(routeId)

            when {
                routeResult is Result.Error -> routeResult
                stopsResult is Result.Error -> stopsResult
                routeResult is Result.Success && stopsResult is Result.Success -> {
                    Result.success(Pair(routeResult.data, stopsResult.data))
                }
                else -> Result.error(
                    AudioCityError.Unknown(message = "Unexpected state in getCompleteRoute")
                )
            }
        } catch (e: Exception) {
            Result.error(
                AudioCityError.Network.ServerError(
                    message = "Failed to fetch complete route: ${e.message}",
                    code = 500,
                    cause = e
                )
            )
        }
    }
}
