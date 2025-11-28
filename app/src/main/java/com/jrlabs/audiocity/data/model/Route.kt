package com.jrlabs.audiocity.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val city: String = "",
    val neighborhood: String = "",
    @get:PropertyName("duration_minutes") @set:PropertyName("duration_minutes")
    var durationMinutes: Int = 0,
    @get:PropertyName("distance_km") @set:PropertyName("distance_km")
    var distanceKm: Double = 0.0,
    val difficulty: String = "easy", // "easy", "medium", "hard"
    @get:PropertyName("num_stops") @set:PropertyName("num_stops")
    var numStops: Int = 0,
    val language: String = "es",
    @get:PropertyName("is_active") @set:PropertyName("is_active")
    var isActive: Boolean = true,
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    var createdAt: String = "",
    @get:PropertyName("updated_at") @set:PropertyName("updated_at")
    var updatedAt: String = "",
    @get:PropertyName("thumbnail_url") @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String = "",
    @get:PropertyName("start_location") @set:PropertyName("start_location")
    var startLocation: RouteLocation = RouteLocation(),
    @get:PropertyName("end_location") @set:PropertyName("end_location")
    var endLocation: RouteLocation = RouteLocation()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Route? {
            return try {
                val data = document.data ?: return null
                Route(
                    id = document.id,
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    city = data["city"] as? String ?: "",
                    neighborhood = data["neighborhood"] as? String ?: "",
                    durationMinutes = (data["duration_minutes"] as? Long)?.toInt() ?: 0,
                    distanceKm = (data["distance_km"] as? Number)?.toDouble() ?: 0.0,
                    difficulty = data["difficulty"] as? String ?: "easy",
                    numStops = (data["num_stops"] as? Long)?.toInt() ?: 0,
                    language = data["language"] as? String ?: "es",
                    isActive = data["is_active"] as? Boolean ?: true,
                    createdAt = data["created_at"] as? String ?: "",
                    updatedAt = data["updated_at"] as? String ?: "",
                    thumbnailUrl = data["thumbnail_url"] as? String ?: "",
                    startLocation = RouteLocation.fromMap(data["start_location"] as? Map<String, Any>),
                    endLocation = RouteLocation.fromMap(data["end_location"] as? Map<String, Any>)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Serializable
data class RouteLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, Any>?): RouteLocation {
            if (map == null) return RouteLocation()
            return RouteLocation(
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                name = map["name"] as? String ?: ""
            )
        }
    }
}
