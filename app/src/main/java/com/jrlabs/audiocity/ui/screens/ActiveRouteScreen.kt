package com.jrlabs.audiocity.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSecondary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.theme.Gray200
import com.jrlabs.audiocity.ui.theme.Gray500
import com.jrlabs.audiocity.ui.theme.White
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveRouteScreen(
    onRouteEnded: () -> Unit,
    viewModel: RouteViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentRoute by viewModel.currentRoute.collectAsState()
    val stops by viewModel.stops.collectAsState()
    val visitedCount by viewModel.visitedStopsCount.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val currentLocation by viewModel.locationService.currentLocation.collectAsState()
    val currentQueueItem by viewModel.currentQueueItem.collectAsState()

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Get walking route points from ViewModel (already calculated)
    val walkingRoutePoints by viewModel.walkingRoutePoints.collectAsState()

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Calculate center of all stops
    val centerLat = stops.map { it.latitude }.average().takeIf { !it.isNaN() } ?: 40.4168
    val centerLng = stops.map { it.longitude }.average().takeIf { !it.isNaN() } ?: -3.7038

    // Get next stop (first unvisited by order)
    val nextStop = stops.filter { !it.hasBeenVisited }.minByOrNull { it.order }

    // Calculate distance to next stop
    val distanceToNextStop = remember(nextStop, currentLocation) {
        val location = currentLocation
        if (nextStop != null && location != null) {
            calculateDistanceMeters(
                location.latitude, location.longitude,
                nextStop.latitude, nextStop.longitude
            )
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Follow user location
    LaunchedEffect(currentLocation, mapLibreMap) {
        currentLocation?.let { location ->
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    16.0
                ),
                1000
            )
        }
    }

    // Update markers and polyline when stops or route points change
    LaunchedEffect(stops, currentQueueItem, mapLibreMap, walkingRoutePoints, currentLocation) {
        mapLibreMap?.let { map ->
            map.clear()

            // Draw walking route polyline (real streets)
            if (walkingRoutePoints.size > 1) {
                map.addPolyline(
                    PolylineOptions()
                        .addAll(walkingRoutePoints)
                        .color(ACPrimary.toArgb())
                        .width(6f)
                )
            }

            // Add user location marker
            currentLocation?.let { location ->
                addUserLocationMarker(context, map, location.latitude, location.longitude)
            }

            // Add markers for stops
            val currentNextStop = stops.filter { !it.hasBeenVisited }.minByOrNull { it.order }
            stops.forEach { stop ->
                val isCurrentlyPlaying = currentQueueItem?.stopId == stop.id
                addActiveRouteMarker(context, map, stop, isCurrentlyPlaying, currentNextStop?.id == stop.id)
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
            mapView?.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapLibre Map
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map

                        // Use OpenFreeMap tiles (free, no API key, good quality)
                        val styleUrl = "https://tiles.openfreemap.org/styles/liberty"

                        map.setStyle(Style.Builder().fromUri(styleUrl)) { _ ->
                            // Set initial camera to center of stops
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(centerLat, centerLng))
                                .zoom(15.0)
                                .build()

                            // Draw walking route polyline (if already loaded)
                            if (walkingRoutePoints.size > 1) {
                                map.addPolyline(
                                    PolylineOptions()
                                        .addAll(walkingRoutePoints)
                                        .color(ACPrimary.toArgb())
                                        .width(6f)
                                )
                            }

                            // Add markers for all stops
                            val currentNextStop = stops.filter { !it.hasBeenVisited }.minByOrNull { it.order }
                            stops.forEach { stop ->
                                val isCurrentlyPlaying = currentQueueItem?.stopId == stop.id
                                addActiveRouteMarker(ctx, map, stop, isCurrentlyPlaying, currentNextStop?.id == stop.id)
                            }
                        }
                    }
                }
            },
            update = { _ -> }
        )

        // Top card - Route info (Transit style)
        RouteInfoCard(
            routeName = currentRoute?.name ?: "Ruta activa",
            visitedCount = visitedCount,
            totalStops = stops.size,
            onClose = {
                viewModel.endRoute()
                onRouteEnded()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .padding(top = 32.dp)
        )

        // Center location FAB - positioned above the bottom card
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    mapLibreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            16.0
                        ),
                        500
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 195.dp),
            containerColor = White,
            contentColor = ACPrimary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar ubicación"
            )
        }

        // Bottom card - Next stop info + controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(White)
                .padding(20.dp)
        ) {
            // Next stop or completed
            if (nextStop != null) {
                NextStopSection(
                    stop = nextStop,
                    distanceMeters = distanceToNextStop,
                    isPlaying = isPlaying,
                    isPaused = isPaused,
                    onPause = { viewModel.pauseAudio() },
                    onResume = { viewModel.resumeAudio() },
                    onStop = { viewModel.stopAudio() },
                    onSkip = { viewModel.skipToNext() }
                )
            } else if (visitedCount == stops.size && stops.isNotEmpty()) {
                RouteCompletedSection(
                    onFinish = {
                        viewModel.endRoute()
                        onRouteEnded()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress dots
            ProgressDotsBar(
                stops = stops,
                nextStopId = nextStop?.id
            )
        }
    }
}

@Composable
private fun RouteInfoCard(
    routeName: String,
    visitedCount: Int,
    totalStops: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing indicator
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(ACPrimary.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ACPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Route info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RUTA EN MARCHA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ACPrimary
                )
                Text(
                    text = routeName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress counter
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$visitedCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ACPrimary
                )
                Text(
                    text = "/$totalStops",
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Gray200)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Gray500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun NextStopSection(
    stop: Stop,
    distanceMeters: Int?,
    isPlaying: Boolean,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Stop info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRÓXIMA PARADA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stop.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Distance badge
            Surface(
                color = ACPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (distanceMeters != null) {
                        val (value, unit) = formatDistanceParts(distanceMeters)
                        Text(
                            text = value,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ACPrimary
                        )
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = ACPrimary.copy(alpha = 0.8f)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = ACPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "caminando",
                            style = MaterialTheme.typography.labelSmall,
                            color = ACPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Audio controls (if playing or paused)
        if (isPlaying || isPaused) {
            Spacer(modifier = Modifier.height(16.dp))
            AudioControlsRow(
                isPlaying = isPlaying,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onSkip = onSkip
            )
        }
    }
}

@Composable
private fun AudioControlsRow(
    isPlaying: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray200.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isPlaying) "Reproduciendo..." else "En pausa",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stop button
            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Gray200
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Detener",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play/Pause button
            FilledIconButton(
                onClick = { if (isPlaying) onPause() else onResume() },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ACPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Skip button
            FilledIconButton(
                onClick = onSkip,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Gray200
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RouteCompletedSection(
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ACSuccess.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ACSuccess,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ruta completada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Has visitado todas las paradas",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ACPrimary
            )
        ) {
            Text("Finalizar ruta")
        }
    }
}

@Composable
private fun ProgressDotsBar(
    stops: List<Stop>,
    nextStopId: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stops.sortedBy { it.order }.forEachIndexed { index, stop ->
            // Dot
            val dotColor = when {
                stop.hasBeenVisited -> ACSuccess
                stop.id == nextStopId -> ACPrimary
                else -> Gray200
            }
            val dotSize = if (stop.id == nextStopId) 14.dp else 10.dp

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                if (stop.hasBeenVisited) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(8.dp)
                    )
                }
            }

            // Line between dots
            if (index < stops.size - 1) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(if (stop.hasBeenVisited) ACSuccess else Gray200)
                )
            }
        }
    }
}

/**
 * Add a custom marker for active route stop
 */
private fun addActiveRouteMarker(
    context: Context,
    map: MapLibreMap,
    stop: Stop,
    isCurrentlyPlaying: Boolean,
    isNextStop: Boolean
) {
    val iconFactory = IconFactory.getInstance(context)

    val markerColor = when {
        isCurrentlyPlaying -> ACSecondary
        stop.hasBeenVisited -> ACSuccess
        isNextStop -> ACPrimary
        else -> Gray500
    }

    val markerBitmap = createActiveRouteMarkerBitmap(
        isPlaying = isCurrentlyPlaying,
        isVisited = stop.hasBeenVisited,
        isNext = isNextStop,
        stopOrder = stop.order,
        color = markerColor.toArgb()
    )

    val icon = iconFactory.fromBitmap(markerBitmap)

    map.addMarker(
        MarkerOptions()
            .position(LatLng(stop.latitude, stop.longitude))
            .title("${stop.order}. ${stop.name}")
            .snippet(if (stop.hasBeenVisited) "Visitado" else if (isNextStop) "Siguiente" else "Pendiente")
            .icon(icon)
    )
}

/**
 * Create a custom marker bitmap with number or checkmark
 */
private fun createActiveRouteMarkerBitmap(
    isPlaying: Boolean,
    isVisited: Boolean,
    isNext: Boolean,
    stopOrder: Int,
    color: Int
): Bitmap {
    val size = if (isNext || isPlaying) 56 else 44
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background circle
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }
    val padding = 2f
    val rect = RectF(padding, padding, size - padding, size - padding)
    canvas.drawRoundRect(rect, size / 2f, size / 2f, paint)

    // White border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = android.graphics.Color.WHITE
        strokeWidth = 3f
    }
    canvas.drawRoundRect(rect, size / 2f, size / 2f, borderPaint)

    if (isVisited) {
        // Draw checkmark
        val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            strokeWidth = 3f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val cx = size / 2f
        val cy = size / 2f
        val checkSize = size / 4f
        canvas.drawLine(cx - checkSize * 0.5f, cy, cx - checkSize * 0.1f, cy + checkSize * 0.4f, checkPaint)
        canvas.drawLine(cx - checkSize * 0.1f, cy + checkSize * 0.4f, cx + checkSize * 0.5f, cy - checkSize * 0.3f, checkPaint)
    } else {
        // Number text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            textSize = if (isNext || isPlaying) 22f else 18f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(stopOrder.toString(), size / 2f, textY, textPaint)
    }

    return bitmap
}

/**
 * Calculate distance between two coordinates in meters using Haversine formula
 */
private fun calculateDistanceMeters(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Int {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (earthRadius * c).toInt()
}

/**
 * Format distance for display
 */
private fun formatDistance(meters: Int): String {
    return when {
        meters < 1000 -> "$meters m"
        else -> String.format("%.1f km", meters / 1000.0)
    }
}

/**
 * Format distance returning value and unit separately
 */
private fun formatDistanceParts(meters: Int): Pair<String, String> {
    return when {
        meters < 1000 -> Pair(meters.toString(), "metros")
        else -> Pair(String.format("%.1f", meters / 1000.0), "km")
    }
}

/**
 * Add user location marker (blue pulsing dot style)
 */
private fun addUserLocationMarker(
    context: Context,
    map: MapLibreMap,
    latitude: Double,
    longitude: Double
) {
    val iconFactory = IconFactory.getInstance(context)
    val markerBitmap = createUserLocationMarkerBitmap()
    val icon = iconFactory.fromBitmap(markerBitmap)

    map.addMarker(
        MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title("Tu ubicación")
            .icon(icon)
    )
}

/**
 * Create user location marker bitmap (blue dot with white border and outer glow)
 */
private fun createUserLocationMarkerBitmap(): Bitmap {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = size / 2f
    val centerY = size / 2f

    // Outer glow (light blue)
    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.argb(60, 66, 133, 244) // Light blue with alpha
    }
    canvas.drawCircle(centerX, centerY, 22f, glowPaint)

    // White border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(centerX, centerY, 14f, borderPaint)

    // Blue center dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.rgb(66, 133, 244) // Google blue
    }
    canvas.drawCircle(centerX, centerY, 10f, dotPaint)

    return bitmap
}
