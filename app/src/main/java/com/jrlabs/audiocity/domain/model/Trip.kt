package com.jrlabs.audiocity.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Domain model representing a planned trip.
 *
 * All properties are immutable. To modify a trip, use the copy() function.
 * This follows functional programming principles and makes the code more predictable.
 */
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val destinationCity: String,
    val destinationCountry: String = "España",
    val selectedRouteIds: List<String> = emptyList(), // Immutable list instead of MutableList
    val createdAt: Date = Date(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val isOfflineAvailable: Boolean = false,
    val lastSyncDate: Date? = null
) {
    /**
     * Number of routes selected for this trip.
     */
    val routeCount: Int
        get() = selectedRouteIds.size

    /**
     * Returns true if the trip has a defined date range.
     */
    val hasDateRange: Boolean
        get() = startDate != null && endDate != null

    /**
     * Returns the trip status based on dates.
     */
    val status: TripStatus
        get() {
            val now = Date()
            return when {
                endDate?.before(now) == true -> TripStatus.PAST
                startDate?.after(now) == true -> TripStatus.FUTURE
                startDate != null && (endDate == null || endDate.after(now)) && startDate.before(now) -> TripStatus.CURRENT
                else -> TripStatus.FUTURE
            }
        }

    /**
     * Returns the formatted date range string.
     */
    fun dateRangeFormatted(): String? {
        if (!hasDateRange) return null
        val formatter = SimpleDateFormat("d MMM", Locale("es", "ES"))
        return "${formatter.format(startDate!!)} - ${formatter.format(endDate!!)}"
    }

    /**
     * Creates a copy of this trip with an additional route.
     */
    fun addRoute(routeId: String): Trip {
        return if (selectedRouteIds.contains(routeId)) {
            this
        } else {
            copy(selectedRouteIds = selectedRouteIds + routeId)
        }
    }

    /**
     * Creates a copy of this trip without the specified route.
     */
    fun removeRoute(routeId: String): Trip {
        return copy(selectedRouteIds = selectedRouteIds - routeId)
    }
}

/**
 * Represents the temporal status of a trip.
 */
enum class TripStatus(val displayName: String) {
    PAST("Pasado"),
    CURRENT("En curso"),
    FUTURE("Próximo")
}

/**
 * Represents an available destination for trips.
 */
data class Destination(
    val id: String = UUID.randomUUID().toString(),
    val city: String,
    val country: String = "España",
    val routeCount: Int = 0,
    val imageUrl: String? = null,
    val isPopular: Boolean = false
)

/**
 * Categories for route sections on the main screen.
 */
enum class RouteCategory(
    val displayName: String,
    val iconName: String,
    val colorName: String
) {
    TOP("Top Rutas", "star", "yellow"),
    TRENDING("De Moda", "flame", "orange"),
    TOURIST("Turísticas", "camera", "blue"),
    CULTURAL("Culturales", "book", "purple"),
    GASTRONOMIC("Gastronómicas", "fork_knife", "red"),
    NATURE("Naturaleza", "leaf", "green")
}

/**
 * Represents a section of routes for the main screen.
 */
data class RouteSection(
    val id: String,
    val category: RouteCategory,
    val routes: List<Route>
)
