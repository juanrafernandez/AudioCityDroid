package com.jrlabs.audiocity.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a completed route in the user's history.
 */
data class RouteHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val routeId: String,
    val routeName: String,
    val routeCity: String,
    val completedAt: Date = Date(),
    val durationMinutes: Int,
    val distanceKm: Double,
    val stopsVisited: Int,
    val totalStops: Int,
    val pointsEarned: Int
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
        get() = stopsVisited >= totalStops

    /**
     * Returns formatted completion date.
     */
    val formattedDate: String
        get() {
            val formatter = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
            return formatter.format(completedAt)
        }

    /**
     * Returns formatted time (for grouping).
     */
    val dateKey: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(completedAt)
        }
}

/**
 * Grouped history entries by date.
 */
data class HistoryGroup(
    val dateKey: String,
    val displayDate: String,
    val entries: List<RouteHistoryEntry>
)
