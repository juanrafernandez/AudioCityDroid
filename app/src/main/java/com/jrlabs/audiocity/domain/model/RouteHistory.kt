package com.jrlabs.audiocity.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Status of a route in history
 */
enum class RouteStatus {
    COMPLETED,  // User visited all stops
    CANCELLED   // User ended route before completing
}

/**
 * Represents a route entry in the user's history.
 */
data class RouteHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val routeId: String,
    val routeName: String,
    val routeCity: String,
    val startedAt: Date = Date(),
    val endedAt: Date = Date(),
    val status: RouteStatus = RouteStatus.COMPLETED,
    val durationMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val stopsVisited: Int = 0,
    val totalStops: Int = 0,
    val pointsEarned: Int = 0
) {
    /**
     * Returns completion percentage.
     */
    val completionPercentage: Int
        get() = if (totalStops > 0) (stopsVisited * 100) / totalStops else 0

    /**
     * Returns true if the route was fully completed.
     */
    val isFullyCompleted: Boolean
        get() = status == RouteStatus.COMPLETED && stopsVisited >= totalStops

    /**
     * Returns formatted start date.
     */
    val formattedDate: String
        get() {
            val formatter = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
            return formatter.format(startedAt)
        }

    /**
     * Returns formatted start time.
     */
    val formattedTime: String
        get() {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            return formatter.format(startedAt)
        }

    /**
     * Returns formatted duration.
     */
    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
        }

    /**
     * Returns status display text.
     */
    val statusText: String
        get() = when (status) {
            RouteStatus.COMPLETED -> "Completada"
            RouteStatus.CANCELLED -> "Cancelada"
        }

    /**
     * Returns formatted time (for grouping).
     */
    val dateKey: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(startedAt)
        }

    // Backwards compatibility alias
    val completedAt: Date
        get() = endedAt
}

/**
 * Grouped history entries by date.
 */
data class HistoryGroup(
    val dateKey: String,
    val displayDate: String,
    val entries: List<RouteHistoryEntry>
)
