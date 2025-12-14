package com.jrlabs.audiocity.ui.screens.routes

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.ui.components.*
import com.jrlabs.audiocity.ui.theme.ACGold
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSecondary
import com.jrlabs.audiocity.ui.theme.ACWarning
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

// Colores de sección (usando sistema de diseño)
val YellowColor = ACGold
val OrangeColor = ACWarning
val RedColor = ACPrimary

/**
 * Pantalla principal de rutas con secciones estilo Wikiloc
 * Equivalente a RoutesListView.swift en iOS
 */
@Composable
fun RoutesListScreen(
    modifier: Modifier = Modifier,
    onRouteSelected: (String) -> Unit,
    onTripSelected: (String) -> Unit = {},
    onPlanTripClick: () -> Unit = {},
    onAllTripsClick: () -> Unit = {},
    onAllRoutesClick: () -> Unit = {},
    viewModel: RouteViewModel
) {
    val routes by viewModel.availableRoutes.collectAsState()
    val isLoading by viewModel.isLoadingRoutes.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val favoriteRouteIds by viewModel.favoriteRouteIds.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    // Función para ordenar por proximidad
    fun sortByProximity(routeList: List<Route>): List<Route> {
        val location = userLocation ?: return routeList
        return routeList.sortedBy { route ->
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                route.startLocation.latitude, route.startLocation.longitude,
                results
            )
            results[0]
        }
    }

    // Rutas filtradas por sección y ordenadas por proximidad
    val favoriteRoutes = remember(routes, favoriteRouteIds, userLocation) {
        sortByProximity(routes.filter { favoriteRouteIds.contains(it.id) })
    }

    val topRoutes = remember(routes, favoriteRouteIds, userLocation) {
        sortByProximity(
            routes.filter { !favoriteRouteIds.contains(it.id) }
        ).take(5)
    }

    // Rutas de moda (mockeadas por ahora, igual que en iOS)
    val trendingRoutes = remember(routes) {
        createMockTrendingRoutes()
    }

    // Viajes próximos (máximo 2)
    val upcomingTrips = remember(trips) {
        trips.filter { !it.isPast }
            .sortedBy { it.startDate ?: java.util.Date(Long.MAX_VALUE) }
            .take(2)
    }

    LaunchedEffect(Unit) {
        viewModel.loadAvailableRoutes()
        // Request location for sorting routes by proximity
        viewModel.requestLocationForSorting()
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = "Descubre tu ciudad",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )

                    // Sección: Mis Viajes
                    TripsSection(
                        trips = upcomingTrips,
                        totalTrips = trips.size,
                        onTripClick = onTripSelected,
                        onPlanClick = onPlanTripClick,
                        onSeeAllClick = onAllTripsClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección: Rutas Favoritas (solo si hay)
                    if (favoriteRoutes.isNotEmpty()) {
                        RoutesCarouselSection(
                            title = "Rutas Favoritas",
                            icon = Icons.Default.Favorite,
                            iconColor = RedColor,
                            routes = favoriteRoutes,
                            onRouteClick = { route ->
                                viewModel.selectRoute(route)
                                onRouteSelected(route.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Sección: Top Rutas
                    RoutesCarouselSection(
                        title = "Top Rutas",
                        icon = Icons.Default.Star,
                        iconColor = YellowColor,
                        routes = topRoutes,
                        onRouteClick = { route ->
                            viewModel.selectRoute(route)
                            onRouteSelected(route.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección: Rutas de Moda
                    RoutesCarouselSection(
                        title = "Rutas de Moda",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = OrangeColor,
                        routes = trendingRoutes,
                        onRouteClick = { route ->
                            viewModel.selectRoute(route)
                            onRouteSelected(route.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón: Todas las Rutas (usando color coral primario)
                    Button(
                        onClick = onAllRoutesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ACPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explorar todas las rutas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Sección de Mis Viajes
 */
@Composable
fun TripsSection(
    trips: List<Trip>,
    totalTrips: Int,
    onTripClick: (String) -> Unit,
    onPlanClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header de sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ACSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Luggage,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ACSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mis Viajes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (totalTrips > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${trips.size} de $totalTrips",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row {
                // Botón Planificar (usando color primario coral)
                Surface(
                    modifier = Modifier.clickable { onPlanClick() },
                    shape = RoundedCornerShape(16.dp),
                    color = ACPrimary
                ) {
                    Text(
                        text = "Planificar",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón Ver todos (si hay más de 2)
                if (totalTrips > 2) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onSeeAllClick) {
                        Text(
                            text = "Ver todos",
                            color = ACPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contenido
        if (trips.isEmpty()) {
            // Estado vacío
            NewTripCard(
                onClick = onPlanClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            // Lista de viajes
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                trips.forEach { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) }
                    )
                }
            }
        }
    }
}

/**
 * Carrusel horizontal de rutas
 */
@Composable
fun RoutesCarouselSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    routes: List<Route>,
    onRouteClick: (Route) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header de sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Carrusel horizontal
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routes) { route ->
                RouteCardCompact(
                    route = route,
                    onClick = { onRouteClick(route) }
                )
            }
        }
    }
}

/**
 * Card de ruta para listas verticales (usada en búsqueda)
 */
@Composable
fun RouteSearchCard(
    route: Route,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de categoría
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getCategoryColor(route.neighborhood).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(route.neighborhood),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = getCategoryColor(route.neighborhood)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${route.city}, ${route.neighborhood}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${route.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " · ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${route.numStops} paradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " · ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    DifficultyBadge(difficulty = route.difficulty)
                }
            }

            // Botón favorito
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (isFavorite) RedColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Crear rutas mock de moda (igual que en iOS)
 */
fun createMockTrendingRoutes(): List<Route> {
    return listOf(
        Route(
            id = "mock-tapas-lavapies",
            name = "Ruta de la Tapa por Lavapiés",
            description = "Descubre los mejores bares de tapas del barrio más multicultural de Madrid",
            city = "Madrid",
            neighborhood = "Lavapiés",
            difficulty = "fácil",
            durationMinutes = 90,
            distanceKm = 2.0,
            numStops = 8
        ),
        Route(
            id = "mock-navidad-madrid",
            name = "Ruta de Navidad",
            description = "Recorre las luces navideñas y mercadillos más bonitos de la ciudad",
            city = "Madrid",
            neighborhood = "Centro",
            difficulty = "fácil",
            durationMinutes = 120,
            distanceKm = 3.5,
            numStops = 10
        ),
        Route(
            id = "mock-black-friday",
            name = "Ruta Black Friday",
            description = "Las mejores tiendas y descuentos en el centro comercial de Madrid",
            city = "Madrid",
            neighborhood = "Centro",
            difficulty = "media",
            durationMinutes = 150,
            distanceKm = 4.0,
            numStops = 12
        )
    )
}
