package com.jrlabs.audiocity.ui.screens.myroutes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.audiocity.domain.model.UserRoute
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.viewmodel.UserRoutesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRoutesScreen(
    modifier: Modifier = Modifier,
    viewModel: UserRoutesViewModel = hiltViewModel(),
    onCreateRoute: () -> Unit = {},
    onRouteSelected: (UserRoute) -> Unit = {}
) {
    val userRoutes by viewModel.userRoutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear", fontWeight = FontWeight.Bold) },
                actions = {
                    if (userRoutes.isNotEmpty()) {
                        androidx.compose.material3.IconButton(onClick = onCreateRoute) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Crear ruta",
                                tint = ACPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRoutes.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onCreateRoute,
                    containerColor = ACPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear ruta",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (userRoutes.isEmpty()) {
            EmptyRoutesView(
                onCreateRoute = onCreateRoute,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            RoutesListView(
                routes = userRoutes,
                onRouteSelected = onRouteSelected,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyRoutesView(
    onCreateRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ACPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = ACPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No tienes rutas creadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crea tu primera ruta y compártela con otros viajeros",
            style = MaterialTheme.typography.bodyMedium,
            color = ACTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateRoute,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Ruta")
        }
    }
}

@Composable
private fun RoutesListView(
    routes: List<UserRoute>,
    onRouteSelected: (UserRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(routes, key = { it.id }) { route ->
            UserRouteCard(
                route = route,
                onClick = { onRouteSelected(route) }
            )
        }
    }
}

@Composable
private fun UserRouteCard(
    route: UserRoute,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = buildString {
                            append(route.city)
                            if (route.neighborhood.isNotEmpty()) {
                                append(", ${route.neighborhood}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextSecondary
                    )
                }

                // Status badge
                StatusBadge(isPublished = route.isPublished)
            }

            // Description
            if (route.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetaBadge(
                    icon = Icons.Default.LocationOn,
                    text = "${route.stops.size} paradas"
                )
                MetaBadge(
                    icon = Icons.Default.Schedule,
                    text = "${route.estimatedDurationMinutes} min"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Updated date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = null,
                    tint = ACTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Actualizada: ${dateFormat.format(route.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isPublished: Boolean) {
    val backgroundColor = if (isPublished) ACSuccess.copy(alpha = 0.1f) else ACTextSecondary.copy(alpha = 0.1f)
    val textColor = if (isPublished) ACSuccess else ACTextSecondary

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = if (isPublished) "Publicada" else "Borrador",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MetaBadge(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ACTextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = ACTextSecondary
        )
    }
}
