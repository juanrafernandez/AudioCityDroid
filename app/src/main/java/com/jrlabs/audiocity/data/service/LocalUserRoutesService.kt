package com.jrlabs.audiocity.data.service

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.domain.model.RouteDifficulty
import com.jrlabs.audiocity.domain.model.UserRoute
import com.jrlabs.audiocity.domain.model.UserStop
import com.jrlabs.audiocity.domain.service.PointsService
import com.jrlabs.audiocity.domain.service.UserRoutesService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class StoredUserRoute(
    val id: String,
    val name: String,
    val description: String,
    val city: String,
    val neighborhood: String,
    val difficulty: String,
    val stops: List<StoredUserStop>,
    val createdAt: Long,
    val updatedAt: Long,
    val isPublished: Boolean,
    val thumbnailUrl: String
)

@Serializable
private data class StoredUserStop(
    val id: String,
    val name: String,
    val description: String,
    val scriptEs: String,
    val order: Int,
    val latitude: Double,
    val longitude: Double,
    val triggerRadiusMeters: Double,
    val imageUrl: String
)

/**
 * Local SharedPreferences implementation of UserRoutesService.
 */
@Singleton
class LocalUserRoutesService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pointsService: PointsService
) : UserRoutesService {

    companion object {
        private const val PREFS_NAME = "audiocity_user_routes"
        private const val KEY_ROUTES = "user_routes"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _userRoutes = MutableStateFlow<List<UserRoute>>(emptyList())
    override val userRoutes: StateFlow<List<UserRoute>> = _userRoutes.asStateFlow()

    private val _publishedRoutes = MutableStateFlow<List<UserRoute>>(emptyList())
    override val publishedRoutes: StateFlow<List<UserRoute>> = _publishedRoutes.asStateFlow()

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        try {
            val routesJson = prefs.getString(KEY_ROUTES, null)
            if (routesJson != null) {
                val stored = json.decodeFromString<List<StoredUserRoute>>(routesJson)
                _userRoutes.value = stored.map { it.toRoute() }
                updatePublishedRoutes()
            }
        } catch (e: Exception) {
            _userRoutes.value = emptyList()
        }
    }

    private fun saveToPrefs() {
        try {
            val stored = _userRoutes.value.map { it.toStored() }
            prefs.edit()
                .putString(KEY_ROUTES, json.encodeToString(stored))
                .apply()
        } catch (e: Exception) {
            // Ignore save errors
        }
    }

    private fun updatePublishedRoutes() {
        _publishedRoutes.value = _userRoutes.value.filter { it.isPublished }
    }

    override fun createRoute(route: UserRoute): UserRoute {
        val newRoute = route.copy(
            createdAt = Date(),
            updatedAt = Date()
        )

        _userRoutes.value = _userRoutes.value + newRoute
        saveToPrefs()

        // Award points for creating route
        if (newRoute.stops.size >= 3) {
            pointsService.awardPointsForCreatingRoute(
                newRoute.id,
                newRoute.name,
                newRoute.stops.size
            )
        }

        return newRoute
    }

    override fun updateRoute(route: UserRoute) {
        _userRoutes.value = _userRoutes.value.map {
            if (it.id == route.id) route.copy(updatedAt = Date()) else it
        }
        updatePublishedRoutes()
        saveToPrefs()
    }

    override fun deleteRoute(routeId: String) {
        _userRoutes.value = _userRoutes.value.filter { it.id != routeId }
        updatePublishedRoutes()
        saveToPrefs()
    }

    override fun getRouteById(routeId: String): UserRoute? {
        return _userRoutes.value.find { it.id == routeId }
    }

    override fun addStopToRoute(routeId: String, stop: UserStop) {
        _userRoutes.value = _userRoutes.value.map { route ->
            if (route.id == routeId) {
                route.addStop(stop)
            } else route
        }
        saveToPrefs()
    }

    override fun removeStopFromRoute(routeId: String, stopId: String) {
        _userRoutes.value = _userRoutes.value.map { route ->
            if (route.id == routeId) {
                route.removeStop(stopId)
            } else route
        }
        saveToPrefs()
    }

    override fun reorderStops(routeId: String, stopIds: List<String>) {
        _userRoutes.value = _userRoutes.value.map { route ->
            if (route.id == routeId) {
                val reorderedStops = stopIds.mapIndexedNotNull { index, stopId ->
                    route.stops.find { it.id == stopId }?.copy(order = index + 1)
                }
                route.copy(stops = reorderedStops, updatedAt = Date())
            } else route
        }
        saveToPrefs()
    }

    override fun publishRoute(routeId: String) {
        val route = getRouteById(routeId) ?: return

        _userRoutes.value = _userRoutes.value.map {
            if (it.id == routeId) it.copy(isPublished = true, updatedAt = Date()) else it
        }
        updatePublishedRoutes()
        saveToPrefs()

        // Award points for publishing
        pointsService.awardPointsForPublishingRoute(route.id, route.name)
    }

    override fun unpublishRoute(routeId: String) {
        _userRoutes.value = _userRoutes.value.map {
            if (it.id == routeId) it.copy(isPublished = false, updatedAt = Date()) else it
        }
        updatePublishedRoutes()
        saveToPrefs()
    }

    private fun StoredUserRoute.toRoute(): UserRoute {
        return UserRoute(
            id = id,
            name = name,
            description = description,
            city = city,
            neighborhood = neighborhood,
            difficulty = RouteDifficulty.fromString(difficulty),
            stops = stops.map { it.toStop() },
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            isPublished = isPublished,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun UserRoute.toStored(): StoredUserRoute {
        return StoredUserRoute(
            id = id,
            name = name,
            description = description,
            city = city,
            neighborhood = neighborhood,
            difficulty = difficulty.value,
            stops = stops.map { it.toStored() },
            createdAt = createdAt.time,
            updatedAt = updatedAt.time,
            isPublished = isPublished,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun StoredUserStop.toStop(): UserStop {
        return UserStop(
            id = id,
            name = name,
            description = description,
            scriptEs = scriptEs,
            order = order,
            latitude = latitude,
            longitude = longitude,
            triggerRadiusMeters = triggerRadiusMeters,
            imageUrl = imageUrl
        )
    }

    private fun UserStop.toStored(): StoredUserStop {
        return StoredUserStop(
            id = id,
            name = name,
            description = description,
            scriptEs = scriptEs,
            order = order,
            latitude = latitude,
            longitude = longitude,
            triggerRadiusMeters = triggerRadiusMeters,
            imageUrl = imageUrl
        )
    }
}
