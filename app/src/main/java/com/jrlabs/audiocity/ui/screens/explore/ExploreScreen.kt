package com.jrlabs.audiocity.ui.screens.explore

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.ui.components.AudioControlBar
import com.jrlabs.audiocity.ui.components.StopInfoCard
import com.jrlabs.audiocity.ui.viewmodel.ExploreViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val allStops by viewModel.allStops.collectAsState()
    val selectedStop by viewModel.selectedStop.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Default to Madrid center
    val defaultLocation = LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    LaunchedEffect(Unit) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.startLocationTracking()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15f
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { viewModel.selectStop(null) }
            ) {
                allStops.forEach { stop ->
                    StopMarker(
                        stop = stop,
                        isSelected = selectedStop?.id == stop.id,
                        onClick = {
                            viewModel.selectStop(stop)
                        }
                    )
                }
            }

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

@Composable
private fun StopMarker(
    stop: Stop,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Marker(
        state = MarkerState(position = LatLng(stop.latitude, stop.longitude)),
        title = stop.name,
        snippet = stop.category,
        onClick = {
            onClick()
            true
        }
    )
}
