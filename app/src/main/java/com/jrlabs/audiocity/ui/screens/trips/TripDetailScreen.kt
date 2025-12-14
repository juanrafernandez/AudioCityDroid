package com.jrlabs.audiocity.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.ui.components.DifficultyBadge
import com.jrlabs.audiocity.ui.components.TripStatusBadge
import com.jrlabs.audiocity.ui.theme.ACSecondary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de detalle de viaje
 * Equivalente a TripDetailView.swift en iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    routes: List<Route>,
    onBackClick: () -> Unit,
    onStartRoute: (String) -> Unit,
    onDeleteTrip: () -> Unit,
    onAddRoutes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
    val tripRoutes = routes.filter { trip.selectedRouteIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.destinationCity) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Añadir rutas") },
                                onClick = {
                                    showMenu = false
                                    onAddRoutes()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar viaje", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con información del viaje
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ACSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Luggage,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = ACSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = trip.destinationCity,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = trip.destinationCountry,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            TripStatusBadge(trip = trip)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fechas
                        if (trip.hasDateRange) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trip.dateRangeFormatted() ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Rutas
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${tripRoutes.size} ruta${if (tripRoutes.size != 1) "s" else ""} seleccionada${if (tripRoutes.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Offline
                        if (trip.isOfflineAvailable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = ACSuccess
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Disponible offline",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ACSuccess
                                )
                            }
                        }
                    }
                }
            }

            // Título de sección
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rutas del viaje",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onAddRoutes) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }
            }

            // Lista de rutas
            if (tripRoutes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay rutas seleccionadas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddRoutes,
                                colors = ButtonDefaults.buttonColors(containerColor = ACSecondary)
                            ) {
                                Text("Añadir rutas")
                            }
                        }
                    }
                }
            } else {
                items(tripRoutes) { route ->
                    TripRouteCard(
                        route = route,
                        onStartClick = { onStartRoute(route.id) }
                    )
                }
            }
        }

        // Diálogo de confirmación para eliminar
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar viaje") },
                text = { Text("¿Estás seguro de que quieres eliminar este viaje? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteTrip()
                        }
                    ) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun TripRouteCard(
    route: Route,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${route.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = " · ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${route.numStops} paradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DifficultyBadge(difficulty = route.difficulty)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = ACSecondary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Comenzar")
            }
        }
    }
}
