package com.jrlabs.audiocity.domain.model

/**
 * Domain model representing a stop/point of interest within a route.
 *
 * This is a pure domain model with no dependencies on external frameworks.
 * All properties are immutable (val) following functional programming best practices.
 */
data class Stop(
    val id: String,
    val routeId: String,
    val order: Int,
    val name: String,
    val description: String,
    val category: StopCategory,
    val latitude: Double,
    val longitude: Double,
    val triggerRadiusMeters: Double,
    val audioDurationSeconds: Int,
    val imageUrl: String,
    val scriptEs: String,
    val funFact: String
) {
    /**
     * Returns the location as a Location object.
     */
    val location: Location
        get() = Location(latitude, longitude, name)

    /**
     * Returns the formatted audio duration.
     */
    val formattedAudioDuration: String
        get() = when {
            audioDurationSeconds < 60 -> "${audioDurationSeconds}s"
            audioDurationSeconds % 60 == 0 -> "${audioDurationSeconds / 60}min"
            else -> "${audioDurationSeconds / 60}:${String.format("%02d", audioDurationSeconds % 60)}"
        }

    /**
     * Checks if this stop has a fun fact available.
     */
    val hasFunFact: Boolean
        get() = funFact.isNotBlank()

    /**
     * Checks if this stop has an audio script available.
     */
    val hasAudioScript: Boolean
        get() = scriptEs.isNotBlank()
}

/**
 * Enum representing stop categories.
 */
enum class StopCategory(val displayName: String, val value: String) {
    HISTORY("Historia", "historia"),
    CULTURE("Cultura", "cultura"),
    ARCHITECTURE("Arquitectura", "arquitectura"),
    ART("Arte", "arte"),
    GASTRONOMY("Gastronomía", "gastronomia"),
    NATURE("Naturaleza", "naturaleza"),
    LANDMARK("Monumento", "monumento"),
    OTHER("Otro", "otro");

    companion object {
        fun fromString(value: String): StopCategory {
            return entries.find { it.value == value.lowercase() } ?: OTHER
        }
    }
}

/**
 * Represents an item in the audio playback queue.
 */
data class AudioQueueItem(
    val id: String,
    val stopId: String,
    val stopName: String,
    val text: String,
    val order: Int
)
