package com.jrlabs.audiocity.domain.model

/**
 * Domain model representing a route/audioguide.
 *
 * This is a pure domain model with no dependencies on external frameworks
 * (no Firebase, no Room annotations). It follows the Single Responsibility Principle.
 *
 * All properties are immutable (val) following functional programming best practices.
 */
data class Route(
    val id: String,
    val name: String,
    val description: String,
    val city: String,
    val neighborhood: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val difficulty: RouteDifficulty,
    val numStops: Int,
    val language: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String,
    val startLocation: Location,
    val endLocation: Location
) {
    /**
     * Returns the formatted duration as a human-readable string.
     */
    val formattedDuration: String
        get() = when {
            durationMinutes < 60 -> "$durationMinutes min"
            durationMinutes % 60 == 0 -> "${durationMinutes / 60}h"
            else -> "${durationMinutes / 60}h ${durationMinutes % 60}min"
        }

    /**
     * Returns the formatted distance.
     */
    val formattedDistance: String
        get() = if (distanceKm < 1) {
            "${(distanceKm * 1000).toInt()}m"
        } else {
            String.format("%.1f km", distanceKm)
        }
}

/**
 * Represents a geographic location with coordinates.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
) {
    companion object {
        val EMPTY = Location(0.0, 0.0, "")
    }
}

/**
 * Enum representing route difficulty levels.
 * Uses sealed approach for type safety and exhaustive when expressions.
 */
enum class RouteDifficulty(val displayName: String, val value: String) {
    EASY("Fácil", "easy"),
    MEDIUM("Media", "medium"),
    HARD("Difícil", "hard");

    companion object {
        fun fromString(value: String): RouteDifficulty {
            return entries.find { it.value == value.lowercase() } ?: EASY
        }
    }
}
