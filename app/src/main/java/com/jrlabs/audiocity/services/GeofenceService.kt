package com.jrlabs.audiocity.services

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.jrlabs.audiocity.data.model.Stop
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient,
    private val locationService: LocationService
) {

    private val _triggeredStop = MutableSharedFlow<Stop>(replay = 0)
    val triggeredStop: SharedFlow<Stop> = _triggeredStop.asSharedFlow()

    private val _nearbyStops = MutableStateFlow<List<Stop>>(emptyList())
    val nearbyStops: StateFlow<List<Stop>> = _nearbyStops.asStateFlow()

    private var currentStops: List<Stop> = emptyList()
    private val visitedStopIds = mutableSetOf<String>()
    private val processedStopIds = mutableSetOf<String>()

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun setupGeofences(stops: List<Stop>) {
        currentStops = stops
        visitedStopIds.clear()
        processedStopIds.clear()

        if (!hasLocationPermission()) return

        // Create geofences for each stop (Android supports up to 100)
        val geofenceList = stops.take(100).map { stop ->
            Geofence.Builder()
                .setRequestId(stop.id)
                .setCircularRegion(
                    stop.latitude,
                    stop.longitude,
                    stop.triggerRadiusMeters.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        if (geofenceList.isEmpty()) return

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun clearGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
        currentStops = emptyList()
        visitedStopIds.clear()
        processedStopIds.clear()
        _nearbyStops.value = emptyList()
    }

    suspend fun checkProximity() {
        val location = locationService.currentLocation.value ?: return

        currentStops.forEach { stop ->
            if (stop.hasBeenVisited || processedStopIds.contains(stop.id)) return@forEach

            val distance = locationService.distanceBetween(
                location.latitude,
                location.longitude,
                stop.latitude,
                stop.longitude
            )

            if (distance <= stop.triggerRadiusMeters) {
                triggerStop(stop)
            }
        }

        // Update nearby stops (within 500 meters)
        val nearby = currentStops.filter { stop ->
            val distance = locationService.distanceBetween(
                location.latitude,
                location.longitude,
                stop.latitude,
                stop.longitude
            )
            distance <= 500
        }.sortedBy { stop ->
            locationService.distanceBetween(
                location.latitude,
                location.longitude,
                stop.latitude,
                stop.longitude
            )
        }
        _nearbyStops.value = nearby
    }

    private suspend fun triggerStop(stop: Stop) {
        if (processedStopIds.contains(stop.id)) return

        processedStopIds.add(stop.id)
        visitedStopIds.add(stop.id)

        // Update the stop's visited status in our local list
        currentStops = currentStops.map {
            if (it.id == stop.id) it.copy(hasBeenVisited = true) else it
        }

        _triggeredStop.emit(stop)
    }

    fun markStopAsVisited(stopId: String) {
        visitedStopIds.add(stopId)
        processedStopIds.add(stopId)
        currentStops = currentStops.map {
            if (it.id == stopId) it.copy(hasBeenVisited = true) else it
        }
    }

    fun getProgress(): Float {
        if (currentStops.isEmpty()) return 0f
        return visitedStopIds.size.toFloat() / currentStops.size.toFloat()
    }

    fun getVisitedCount(): Int = visitedStopIds.size

    fun getNextUnvisitedStop(): Stop? {
        return currentStops
            .filter { !it.hasBeenVisited && !visitedStopIds.contains(it.id) }
            .minByOrNull { it.order }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && backgroundLocation
    }

    companion object {
        // This will be called from the BroadcastReceiver
        var onGeofenceEntered: ((String) -> Unit)? = null
    }
}
