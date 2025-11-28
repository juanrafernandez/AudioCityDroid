package com.jrlabs.audiocity.ui.screens.routes

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.ui.theme.BrandBlue
import com.jrlabs.audiocity.ui.theme.Gray500
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

@Composable
fun RoutesListScreen(
    modifier: Modifier = Modifier,
    onRouteSelected: (String) -> Unit,
    viewModel: RouteViewModel
) {
    val routes by viewModel.availableRoutes.collectAsState()
    val isLoading by viewModel.isLoadingRoutes.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAvailableRoutes()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error al cargar rutas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }
            routes.isEmpty() -> {
                Text(
                    text = "No hay rutas disponibles",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(routes) { route ->
                        RouteCard(
                            route = route,
                            onClick = {
                                viewModel.selectRoute(route)
                                onRouteSelected(route.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${route.neighborhood}, ${route.city}",
                style = MaterialTheme.typography.bodyMedium,
                color = BrandBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = route.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RouteInfoChip(
                    icon = Icons.Default.Place,
                    text = "${route.numStops} paradas"
                )
                RouteInfoChip(
                    icon = Icons.Default.AccessTime,
                    text = "${route.durationMinutes} min"
                )
                RouteInfoChip(
                    icon = Icons.Default.LocationOn,
                    text = "${route.distanceKm} km"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DifficultyBadge(difficulty = route.difficulty)
        }
    }
}

@Composable
private fun RouteInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Gray500
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@Composable
private fun DifficultyBadge(difficulty: String) {
    val (text, color) = when (difficulty.lowercase()) {
        "easy" -> "Fácil" to MaterialTheme.colorScheme.primary
        "medium" -> "Moderada" to MaterialTheme.colorScheme.secondary
        "hard" -> "Difícil" to MaterialTheme.colorScheme.error
        else -> difficulty to MaterialTheme.colorScheme.primary
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}
