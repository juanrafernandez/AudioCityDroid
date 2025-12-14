package com.jrlabs.audiocity.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.R
import com.jrlabs.audiocity.data.model.Stop
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSecondary
import com.jrlabs.audiocity.ui.theme.Gray500
import com.jrlabs.audiocity.ui.theme.White
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onStartRoute: () -> Unit,
    viewModel: RouteViewModel
) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    val stops by viewModel.stops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isCalculatingRoute by viewModel.isCalculatingRoute.collectAsState()

    var showOptimizationDialog by remember { mutableStateOf(false) }
    var isCheckingLocation by remember { mutableStateOf(false) }
    var pendingOptimization by remember { mutableStateOf(false) }

    // Request location when screen loads
    LaunchedEffect(Unit) {
        viewModel.requestLocationForSorting()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRoute?.name ?: "Detalle de ruta") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.backToRoutesList()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                currentRoute?.let { route ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Route info header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = route.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    InfoItem(
                                        icon = Icons.Default.Place,
                                        value = "${route.numStops}",
                                        label = "Paradas"
                                    )
                                    InfoItem(
                                        icon = Icons.Default.AccessTime,
                                        value = "${route.durationMinutes}",
                                        label = "Minutos"
                                    )
                                    InfoItem(
                                        icon = Icons.Default.LocationOn,
                                        value = "${route.distanceKm}",
                                        label = "Km"
                                    )
                                }
                            }
                        }

                        // Stops list
                        Text(
                            text = "Paradas de la ruta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(stops) { index, stop ->
                                StopListItem(
                                    stop = stop,
                                    index = index + 1,
                                    isLast = index == stops.lastIndex
                                )
                            }
                        }

                        // Start route button
                        Button(
                            onClick = {
                                isCheckingLocation = true
                                // Request fresh location
                                viewModel.requestLocationForSorting()
                                // Give it a moment to update, then check
                                isCheckingLocation = false

                                // Check if optimization should be suggested
                                if (viewModel.shouldSuggestRouteOptimization()) {
                                    showOptimizationDialog = true
                                } else {
                                    // Start calculating route without optimization
                                    pendingOptimization = false
                                    showOptimizationDialog = true // Show dialog with loading
                                    viewModel.prepareAndStartRoute(optimized = false) {
                                        showOptimizationDialog = false
                                        onStartRoute()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ACPrimary),
                            enabled = !isCheckingLocation && !isCalculatingRoute
                        ) {
                            if (isCheckingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.start_route),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Optimization/Loading Dialog
    if (showOptimizationDialog) {
        val nearestStopInfo = viewModel.getNearestStopInfo()
        val shouldShowOptimizationChoice = nearestStopInfo != null && !isCalculatingRoute

        AlertDialog(
            onDismissRequest = {
                if (!isCalculatingRoute) {
                    showOptimizationDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = if (isCalculatingRoute) ACPrimary else ACSecondary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = if (isCalculatingRoute) "Calculando ruta..." else "Optimizar ruta",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCalculatingRoute) {
                        // Loading state
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = ACPrimary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Trazando el mejor camino por las calles...",
                            textAlign = TextAlign.Center,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esto puede tardar unos segundos",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Gray500
                        )
                    } else if (shouldShowOptimizationChoice) {
                        // Optimization choice
                        Text(
                            text = "Hay una parada más cercana a tu ubicación actual.",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        nearestStopInfo?.let { info ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = ACSecondary.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = ACSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = info.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "A ${info.distanceMeters} metros de ti",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray500
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "¿Quieres empezar por la parada más cercana?",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (!isCalculatingRoute && shouldShowOptimizationChoice) {
                    Button(
                        onClick = {
                            pendingOptimization = true
                            viewModel.prepareAndStartRoute(optimized = true) {
                                showOptimizationDialog = false
                                onStartRoute()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ACSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sí, optimizar")
                    }
                }
            },
            dismissButton = {
                if (!isCalculatingRoute && shouldShowOptimizationChoice) {
                    OutlinedButton(
                        onClick = {
                            pendingOptimization = false
                            viewModel.prepareAndStartRoute(optimized = false) {
                                showOptimizationDialog = false
                                onStartRoute()
                            }
                        }
                    ) {
                        Text("No, orden original")
                    }
                }
            }
        )
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ACPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@Composable
private fun StopListItem(
    stop: Stop,
    index: Int,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Number indicator
        Surface(
            shape = CircleShape,
            color = ACPrimary,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$index",
                    color = White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stop.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stop.category,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            if (stop.funFact.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stop.funFact,
                    style = MaterialTheme.typography.bodySmall,
                    color = ACPrimary
                )
            }
        }
    }
}
