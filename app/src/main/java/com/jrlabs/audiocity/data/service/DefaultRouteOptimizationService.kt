package com.jrlabs.audiocity.data.service

import android.location.Location
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.RouteOptimizationService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of RouteOptimizationService.
 * Calculates optimal route order based on user location.
 */
@Singleton
class DefaultRouteOptimizationService @Inject constructor() : RouteOptimizationService {

    override fun shouldSuggestOptimization(
        stops: List<Stop>,
        userLocation: Location
    ): Boolean {
        if (stops.isEmpty()) return false

        val nearestInfo = getNearestStopInfo(stops, userLocation)
        return nearestInfo != null && !nearestInfo.isFirstInOrder
    }

    override fun getNearestStopInfo(
        stops: List<Stop>,
        userLocation: Location
    ): RouteOptimizationService.NearestStopInfo? {
        if (stops.isEmpty()) return null

        val sortedByOrder = stops.sortedBy { it.order }
        val firstStop = sortedByOrder.first()

        var nearestStop = firstStop
        var nearestDistance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            firstStop.latitude, firstStop.longitude
        )

        for (stop in stops) {
            val distance = calculateDistance(
                userLocation.latitude, userLocation.longitude,
                stop.latitude, stop.longitude
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestStop = stop
            }
        }

        return RouteOptimizationService.NearestStopInfo(
            stop = nearestStop,
            distanceMeters = nearestDistance,
            isFirstInOrder = nearestStop.id == firstStop.id
        )
    }

    override fun optimizeRoute(
        stops: List<Stop>,
        userLocation: Location
    ): List<Stop> {
        if (stops.isEmpty()) return stops

        val nearestInfo = getNearestStopInfo(stops, userLocation) ?: return stops

        // Find the index of the nearest stop in the original order
        val sortedByOrder = stops.sortedBy { it.order }
        val nearestIndex = sortedByOrder.indexOfFirst { it.id == nearestInfo.stop.id }

        if (nearestIndex <= 0) return sortedByOrder

        // Reorder: start from nearest, then continue in original order
        val reordered = mutableListOf<Stop>()

        // Add from nearest to end
        for (i in nearestIndex until sortedByOrder.size) {
            reordered.add(sortedByOrder[i])
        }

        // Add from start to nearest (circular)
        for (i in 0 until nearestIndex) {
            reordered.add(sortedByOrder[i])
        }

        // Update order numbers
        return reordered.mapIndexed { index, stop ->
            // Create a new stop with updated order
            // Since Stop is immutable, we need to use reflection or create manually
            Stop(
                id = stop.id,
                routeId = stop.routeId,
                order = index + 1,
                name = stop.name,
                description = stop.description,
                category = stop.category,
                latitude = stop.latitude,
                longitude = stop.longitude,
                triggerRadiusMeters = stop.triggerRadiusMeters,
                audioDurationSeconds = stop.audioDurationSeconds,
                imageUrl = stop.imageUrl,
                scriptEs = stop.scriptEs,
                funFact = stop.funFact
            )
        }
    }

    override fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
