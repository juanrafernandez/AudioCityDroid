package com.jrlabs.audiocity.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.service.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FusedLocationProvider implementation of LocationService.
 * Follows Interface Segregation Principle - implements only LocationService methods.
 */
@Singleton
class FusedLocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    override val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var locationCallback: LocationCallback? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L
    ).apply {
        setMinUpdateDistanceMeters(5f)
        setMinUpdateIntervalMillis(2000L)
    }.build()

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasBackgroundLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun startTracking() {
        if (!hasLocationPermission()) return
        if (_isTracking.value) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _currentLocation.value = location
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isTracking.value = true
        } catch (e: SecurityException) {
            // Permission denied at runtime
        }
    }

    override fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        _isTracking.value = false
    }

    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    override fun distanceTo(stop: Stop): Float? {
        val currentLoc = _currentLocation.value ?: return null
        return distanceBetween(
            currentLoc.latitude,
            currentLoc.longitude,
            stop.latitude,
            stop.longitude
        )
    }

    override fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
