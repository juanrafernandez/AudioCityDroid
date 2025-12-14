package com.jrlabs.audiocity.ui.screens.explore

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.ui.components.AudioControlBar
import com.jrlabs.audiocity.ui.components.StopInfoCard
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.viewmodel.ExploreViewModel
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()

    val allStops by viewModel.allStops.collectAsState()
    val selectedStop by viewModel.selectedStop.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request permissions and start tracking
    LaunchedEffect(Unit) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.startLocationTracking()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Update camera when location changes
    LaunchedEffect(currentLocation, mapLibreMap) {
        currentLocation?.let { location ->
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    15.0
                ),
                1000
            )
        }
    }

    // Update markers when stops change
    LaunchedEffect(allStops, mapLibreMap, selectedStop) {
        mapLibreMap?.let { map ->
            map.clear()
            allStops.forEach { stop ->
                addStopMarker(context, map, stop, stop.id == selectedStop?.id, stop.hasBeenVisited)
            }
        }
    }

    // Lifecycle handling for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView?.onStart()
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_STOP -> mapView?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopLocationTracking()
            mapView?.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // MapLibre Map using AndroidView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        onCreate(null)
                        getMapAsync { map ->
                            mapLibreMap = map

                            // Use OpenFreeMap tiles (free, no API key required, good quality)
                            val styleUrl = "https://tiles.openfreemap.org/styles/liberty"
                            map.setStyle(Style.Builder().fromUri(styleUrl)) { _ ->
                                // Set default camera to Madrid
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(40.4168, -3.7038))
                                    .zoom(14.0)
                                    .build()

                                // Add markers for all stops
                                allStops.forEach { stop ->
                                    addStopMarker(ctx, map, stop, stop.id == selectedStop?.id, stop.hasBeenVisited)
                                }
                            }

                            // Handle marker clicks
                            map.setOnMarkerClickListener { marker ->
                                val stopId = marker.snippet
                                val stop = allStops.find { it.id == stopId }
                                stop?.let { viewModel.selectStop(it) }
                                true
                            }

                            // Handle map clicks (deselect)
                            map.addOnMapClickListener {
                                viewModel.selectStop(null)
                                true
                            }
                        }
                    }
                },
                update = { view ->
                    // Update is handled via LaunchedEffects above
                }
            )

            // Selected stop info card
            selectedStop?.let { stop ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    StopInfoCard(
                        stop = stop,
                        routeName = viewModel.getRouteForStop(stop)?.name,
                        onPlayClick = { viewModel.playStop(stop) },
                        onClose = { viewModel.selectStop(null) }
                    )

                    if (isPlaying || isPaused) {
                        AudioControlBar(
                            isPlaying = isPlaying,
                            isPaused = isPaused,
                            onPause = { viewModel.pauseAudio() },
                            onResume = { viewModel.resumeAudio() },
                            onStop = { viewModel.stopAudio() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add a custom marker for a stop
 */
private fun addStopMarker(
    context: Context,
    map: MapLibreMap,
    stop: Stop,
    isSelected: Boolean,
    isVisited: Boolean
) {
    val iconFactory = IconFactory.getInstance(context)

    // Create custom marker bitmap
    val markerBitmap = createMarkerBitmap(
        isSelected = isSelected,
        isVisited = isVisited
    )

    val icon = iconFactory.fromBitmap(markerBitmap)

    map.addMarker(
        MarkerOptions()
            .position(LatLng(stop.latitude, stop.longitude))
            .title(stop.name)
            .snippet(stop.id) // Store stop ID for click handling
            .icon(icon)
    )
}

/**
 * Create a custom marker bitmap with Apple-like rounded style
 */
private fun createMarkerBitmap(
    isSelected: Boolean,
    isVisited: Boolean
): Bitmap {
    val size = if (isSelected) 48 else 36
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = when {
            isVisited -> ACSuccess.toArgb()
            isSelected -> ACPrimary.toArgb()
            else -> ACPrimary.copy(alpha = 0.8f).toArgb()
        }
    }

    // Draw rounded marker (pill shape)
    val padding = 2f
    val rect = RectF(padding, padding, size - padding, size - padding)
    canvas.drawRoundRect(rect, size / 2f, size / 2f, paint)

    // Draw white border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = android.graphics.Color.WHITE
        strokeWidth = 3f
    }
    canvas.drawRoundRect(rect, size / 2f, size / 2f, borderPaint)

    // Draw inner dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 6f, dotPaint)

    return bitmap
}
