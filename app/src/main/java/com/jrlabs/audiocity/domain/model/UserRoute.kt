package com.jrlabs.audiocity.domain.model

import java.util.Date
import java.util.UUID

/**
 * Represents a route created by the user (UGC - User Generated Content).
 */
data class UserRoute(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val city: String,
    val neighborhood: String = "",
    val difficulty: RouteDifficulty = RouteDifficulty.EASY,
    val stops: List<UserStop> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isPublished: Boolean = false,
    val thumbnailUrl: String = ""
) {
    val numStops: Int
        get() = stops.size

    /**
     * Estimated duration based on number of stops.
     * Assumes ~5 minutes per stop + walking time.
     */
    val estimatedDurationMinutes: Int
        get() = stops.size * 5 + (stops.size - 1) * 3

    /**
     * Creates a copy with a new stop added.
     */
    fun addStop(stop: UserStop): UserRoute {
        val newStops = stops + stop.copy(order = stops.size + 1)
        return copy(stops = newStops, updatedAt = Date())
    }

    /**
     * Creates a copy with a stop removed.
     */
    fun removeStop(stopId: String): UserRoute {
        val newStops = stops.filter { it.id != stopId }
            .mapIndexed { index, stop -> stop.copy(order = index + 1) }
        return copy(stops = newStops, updatedAt = Date())
    }

    /**
     * Converts to a regular Route for preview/testing.
     */
    fun toRoute(): Route {
        return Route(
            id = id,
            name = name,
            description = description,
            city = city,
            neighborhood = neighborhood,
            durationMinutes = estimatedDurationMinutes,
            distanceKm = calculateTotalDistance(),
            difficulty = difficulty,
            numStops = numStops,
            language = "es",
            isActive = isPublished,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            thumbnailUrl = thumbnailUrl,
            startLocation = stops.firstOrNull()?.let { Location(it.latitude, it.longitude, it.name) }
                ?: Location.EMPTY,
            endLocation = stops.lastOrNull()?.let { Location(it.latitude, it.longitude, it.name) }
                ?: Location.EMPTY
        )
    }

    private fun calculateTotalDistance(): Double {
        if (stops.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until stops.size - 1) {
            val current = stops[i]
            val next = stops[i + 1]
            total += haversineDistance(
                current.latitude, current.longitude,
                next.latitude, next.longitude
            )
        }
        return total
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

/**
 * Represents a stop in a user-created route.
 */
data class UserStop(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val scriptEs: String = "",
    val order: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val triggerRadiusMeters: Double = 30.0,
    val imageUrl: String = ""
) {
    /**
     * Converts to a regular Stop.
     */
    fun toStop(routeId: String): Stop {
        return Stop(
            id = id,
            routeId = routeId,
            order = order,
            name = name,
            description = description,
            category = StopCategory.OTHER,
            latitude = latitude,
            longitude = longitude,
            triggerRadiusMeters = triggerRadiusMeters,
            audioDurationSeconds = (scriptEs.length / 15), // Estimate: ~15 chars per second
            imageUrl = imageUrl,
            scriptEs = scriptEs,
            funFact = ""
        )
    }
}
