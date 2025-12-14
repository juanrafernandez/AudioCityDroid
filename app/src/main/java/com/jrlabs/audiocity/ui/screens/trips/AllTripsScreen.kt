package com.jrlabs.audiocity.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.ui.components.TripCard
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSecondary
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de todos los viajes (pasados, actuales y futuros)
 * Equivalente a AllTripsView.swift en iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTripsScreen(
    trips: List<Trip>,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onPlanTripClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Agrupar viajes por estado
    val currentTrips = trips.filter { it.isCurrent }
    val futureTrips = trips.filter { it.isFuture && !it.isCurrent }
    val pastTrips = trips.filter { it.isPast }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todos los Viajes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onPlanTripClick) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo viaje")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPlanTripClick,
                containerColor = ACSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Planificar viaje",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (trips.isEmpty()) {
            // Estado vacío
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Luggage,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No tienes viajes planificados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Planifica tu próximo viaje y descubre nuevas rutas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onPlanTripClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ACSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Planificar viaje")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Viajes en curso
                if (currentTrips.isNotEmpty()) {
                    item {
                        TripSectionHeader(
                            title = "En curso",
                            count = currentTrips.size,
                            iconColor = Color(0xFF4CAF50) // Green
                        )
                    }
                    items(currentTrips) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
                }

                // Viajes futuros
                if (futureTrips.isNotEmpty()) {
                    item {
                        if (currentTrips.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        TripSectionHeader(
                            title = "Próximos",
                            count = futureTrips.size,
                            iconColor = Color(0xFF2196F3) // Blue
                        )
                    }
                    items(futureTrips) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
                }

                // Viajes pasados
                if (pastTrips.isNotEmpty()) {
                    item {
                        if (currentTrips.isNotEmpty() || futureTrips.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        TripSectionHeader(
                            title = "Pasados",
                            count = pastTrips.size,
                            iconColor = Color.Gray
                        )
                    }
                    items(pastTrips) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
                }

                // Espacio extra para el FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
fun TripSectionHeader(
    title: String,
    count: Int,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(iconColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
