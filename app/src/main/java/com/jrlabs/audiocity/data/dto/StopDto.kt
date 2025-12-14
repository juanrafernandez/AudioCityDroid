package com.jrlabs.audiocity.data.dto

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object for Stop from Firebase.
 */
data class StopDto(
    val id: String = "",
    @get:PropertyName("route_id") @set:PropertyName("route_id")
    var routeId: String = "",
    val order: Int = 0,
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @get:PropertyName("trigger_radius_meters") @set:PropertyName("trigger_radius_meters")
    var triggerRadiusMeters: Double = 30.0,
    @get:PropertyName("audio_duration_seconds") @set:PropertyName("audio_duration_seconds")
    var audioDurationSeconds: Int = 0,
    @get:PropertyName("image_url") @set:PropertyName("image_url")
    var imageUrl: String = "",
    @get:PropertyName("script_es") @set:PropertyName("script_es")
    var scriptEs: String = "",
    @get:PropertyName("fun_fact") @set:PropertyName("fun_fact")
    var funFact: String = ""
) {
    companion object {
        /**
         * Creates a StopDto from a Firestore document.
         */
        fun fromDocument(document: DocumentSnapshot): StopDto? {
            return try {
                val data = document.data ?: return null
                StopDto(
                    id = document.id,
                    routeId = data["route_id"] as? String ?: "",
                    order = (data["order"] as? Long)?.toInt() ?: 0,
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    category = data["category"] as? String ?: "",
                    latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    triggerRadiusMeters = (data["trigger_radius_meters"] as? Number)?.toDouble() ?: 30.0,
                    audioDurationSeconds = (data["audio_duration_seconds"] as? Long)?.toInt() ?: 0,
                    imageUrl = data["image_url"] as? String ?: "",
                    scriptEs = data["script_es"] as? String ?: "",
                    funFact = data["fun_fact"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
