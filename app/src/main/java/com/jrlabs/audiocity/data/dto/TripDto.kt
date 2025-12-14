package com.jrlabs.audiocity.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Trip.
 * Used for local persistence with kotlinx.serialization.
 */
@Serializable
data class TripDto(
    val id: String,
    val destinationCity: String,
    val destinationCountry: String = "España",
    val selectedRouteIds: List<String> = emptyList(),
    val createdAt: Long, // Unix timestamp
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isOfflineAvailable: Boolean = false,
    val lastSyncDate: Long? = null
)

/**
 * Data Transfer Object for Destination.
 */
@Serializable
data class DestinationDto(
    val id: String,
    val city: String,
    val country: String = "España",
    val routeCount: Int = 0,
    val imageUrl: String? = null,
    val isPopular: Boolean = false
)
