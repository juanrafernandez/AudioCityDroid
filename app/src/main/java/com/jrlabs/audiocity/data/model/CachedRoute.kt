package com.jrlabs.audiocity.data.model

import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat
import java.util.Date
import java.util.UUID

/**
 * Representa una ruta guardada para uso offline
 */
data class CachedRoute(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val route: Route,
    val stops: List<Stop>,
    val cachedAt: Date = Date(),
    val mapTilesPath: String? = null,
    val audioFilesPath: String? = null,
    val totalSizeBytes: Long = 0
) {
    val formattedSize: String
        get() {
            val kb = totalSizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1) {
                DecimalFormat("#.##").format(mb) + " MB"
            } else {
                DecimalFormat("#.##").format(kb) + " KB"
            }
        }

    val mapRegion: MapRegion
        get() = MapRegion.fromStops(stops)
}

/**
 * Región del mapa calculada a partir de las paradas
 */
data class MapRegion(
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double
) {
    val centerLatitude: Double
        get() = (minLatitude + maxLatitude) / 2

    val centerLongitude: Double
        get() = (minLongitude + maxLongitude) / 2

    val latitudeDelta: Double
        get() = (maxLatitude - minLatitude) * 1.1 // 10% padding

    val longitudeDelta: Double
        get() = (maxLongitude - minLongitude) * 1.1 // 10% padding

    val center: LatLng
        get() = LatLng(centerLatitude, centerLongitude)

    companion object {
        fun fromStops(stops: List<Stop>): MapRegion {
            if (stops.isEmpty()) {
                return MapRegion(0.0, 0.0, 0.0, 0.0)
            }

            val latitudes = stops.map { it.latitude }
            val longitudes = stops.map { it.longitude }

            return MapRegion(
                minLatitude = latitudes.minOrNull() ?: 0.0,
                maxLatitude = latitudes.maxOrNull() ?: 0.0,
                minLongitude = longitudes.minOrNull() ?: 0.0,
                maxLongitude = longitudes.maxOrNull() ?: 0.0
            )
        }
    }
}

/**
 * Estado de descarga del caché
 */
enum class CacheDownloadStatus {
    NOT_STARTED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PARTIALLY_COMPLETED
}

/**
 * Progreso de descarga de caché para un viaje
 */
data class TripCacheProgress(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val totalRoutes: Int,
    val cachedRoutes: Int = 0,
    val currentDownloadingRoute: String? = null,
    val downloadProgress: Double = 0.0,
    val status: CacheDownloadStatus = CacheDownloadStatus.NOT_STARTED,
    val errorMessage: String? = null
) {
    val isComplete: Boolean
        get() = status == CacheDownloadStatus.COMPLETED

    val progressPercentage: Int
        get() = if (totalRoutes > 0) {
            ((cachedRoutes.toDouble() / totalRoutes) * 100).toInt()
        } else 0

    val statusDescription: String
        get() = when (status) {
            CacheDownloadStatus.NOT_STARTED -> "No iniciado"
            CacheDownloadStatus.DOWNLOADING -> "Descargando... ${progressPercentage}%"
            CacheDownloadStatus.COMPLETED -> "Completado"
            CacheDownloadStatus.FAILED -> errorMessage ?: "Error en la descarga"
            CacheDownloadStatus.PARTIALLY_COMPLETED -> "Parcialmente completado (${progressPercentage}%)"
        }
}
