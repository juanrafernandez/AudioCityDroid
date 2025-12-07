package com.jrlabs.audiocity.data.model

import java.util.Date
import java.util.UUID

/**
 * Representa un viaje planificado por el usuario
 */
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val destinationCity: String,
    val destinationCountry: String = "España",
    val selectedRouteIds: MutableList<String> = mutableListOf(),
    val createdAt: Date = Date(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val isOfflineAvailable: Boolean = false,
    val lastSyncDate: Date? = null
) {
    val routeCount: Int
        get() = selectedRouteIds.size

    val hasDateRange: Boolean
        get() = startDate != null && endDate != null

    val isPast: Boolean
        get() = endDate?.let { it.before(Date()) } ?: false

    val isCurrent: Boolean
        get() {
            val now = Date()
            return startDate?.let { start ->
                endDate?.let { end ->
                    now.after(start) && now.before(end)
                } ?: now.after(start)
            } ?: false
        }

    val isFuture: Boolean
        get() = startDate?.let { it.after(Date()) } ?: true

    fun dateRangeFormatted(): String? {
        if (!hasDateRange) return null
        val formatter = java.text.SimpleDateFormat("d MMM", java.util.Locale("es", "ES"))
        return "${formatter.format(startDate!!)} - ${formatter.format(endDate!!)}"
    }
}

/**
 * Representa un destino disponible para viajes
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
 * Categorías de rutas para secciones
 */
enum class RouteCategory(
    val displayName: String,
    val icon: String,
    val colorName: String
) {
    TOP("Top Rutas", "star", "yellow"),
    TRENDING("De Moda", "flame", "orange"),
    TOURIST("Turísticas", "camera", "blue"),
    CULTURAL("Culturales", "book", "purple"),
    GASTRONOMIC("Gastronómicas", "fork.knife", "red"),
    NATURE("Naturaleza", "leaf", "green")
}

/**
 * Sección de rutas para la pantalla principal
 */
data class RouteSection(
    val id: String,
    val category: RouteCategory,
    val routes: List<Route>
)
