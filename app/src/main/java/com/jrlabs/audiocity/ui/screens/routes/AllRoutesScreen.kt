package com.jrlabs.audiocity.ui.screens.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.ui.components.DifficultyBadge
import com.jrlabs.audiocity.ui.components.PurpleColor
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

/**
 * Pantalla de todas las rutas con buscador y filtros
 * Equivalente a AllRoutesView.swift en iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRoutesScreen(
    modifier: Modifier = Modifier,
    onRouteSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RouteViewModel
) {
    val routes by viewModel.availableRoutes.collectAsState()
    val favoriteRouteIds by viewModel.favoriteRouteIds.collectAsState()

    // Estados de búsqueda y filtros
    var searchText by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.NAME) }

    // Obtener ciudades únicas
    val cities = remember(routes) {
        routes.map { it.city }.distinct().sorted()
    }

    // Filtrar y ordenar rutas
    val filteredRoutes = remember(routes, searchText, selectedDifficulty, selectedCity, sortOption) {
        var result = routes

        // Filtro de búsqueda
        if (searchText.isNotBlank()) {
            val searchLower = searchText.lowercase()
            result = result.filter { route ->
                route.name.lowercase().contains(searchLower) ||
                route.description.lowercase().contains(searchLower) ||
                route.city.lowercase().contains(searchLower) ||
                route.neighborhood.lowercase().contains(searchLower)
            }
        }

        // Filtro de dificultad
        selectedDifficulty?.let { difficulty ->
            result = result.filter { it.difficulty.lowercase() == difficulty.lowercase() }
        }

        // Filtro de ciudad
        selectedCity?.let { city ->
            result = result.filter { it.city == city }
        }

        // Ordenar
        result = when (sortOption) {
            SortOption.NAME -> result.sortedBy { it.name }
            SortOption.DURATION -> result.sortedBy { it.durationMinutes }
            SortOption.DISTANCE -> result.sortedBy { it.distanceKm }
            SortOption.STOPS -> result.sortedByDescending { it.numStops }
        }

        result
    }

    val hasActiveFilters = selectedDifficulty != null || selectedCity != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las Rutas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            SearchBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filtros
            FiltersRow(
                selectedDifficulty = selectedDifficulty,
                onDifficultyChange = { selectedDifficulty = it },
                selectedCity = selectedCity,
                onCityChange = { selectedCity = it },
                cities = cities,
                sortOption = sortOption,
                onSortChange = { sortOption = it },
                hasActiveFilters = hasActiveFilters,
                onClearFilters = {
                    selectedDifficulty = null
                    selectedCity = null
                }
            )

            // Contador de resultados
            Text(
                text = "${filteredRoutes.size} ruta${if (filteredRoutes.size != 1) "s" else ""} encontrada${if (filteredRoutes.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista de rutas
            if (filteredRoutes.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron rutas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Intenta con otros filtros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRoutes) { route ->
                        RouteSearchCard(
                            route = route,
                            isFavorite = favoriteRouteIds.contains(route.id),
                            onClick = {
                                viewModel.selectRoute(route)
                                onRouteSelected(route.id)
                            },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(route.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de búsqueda
 */
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchText.isEmpty()) {
                    Text(
                        text = "Buscar rutas...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchTextChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Fila de filtros
 */
@Composable
fun FiltersRow(
    selectedDifficulty: String?,
    onDifficultyChange: (String?) -> Unit,
    selectedCity: String?,
    onCityChange: (String?) -> Unit,
    cities: List<String>,
    sortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    var showDifficultyMenu by remember { mutableStateOf(false) }
    var showCityMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filtro de dificultad
        item {
            Box {
                FilterChip(
                    selected = selectedDifficulty != null,
                    onClick = { showDifficultyMenu = true },
                    label = { Text(selectedDifficulty ?: "Dificultad") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenu(
                    expanded = showDifficultyMenu,
                    onDismissRequest = { showDifficultyMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            onDifficultyChange(null)
                            showDifficultyMenu = false
                        }
                    )
                    listOf("Fácil" to "fácil", "Media" to "media", "Difícil" to "difícil").forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onDifficultyChange(value)
                                showDifficultyMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Filtro de ciudad
        item {
            Box {
                FilterChip(
                    selected = selectedCity != null,
                    onClick = { showCityMenu = true },
                    label = { Text(selectedCity ?: "Ciudad") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenu(
                    expanded = showCityMenu,
                    onDismissRequest = { showCityMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            onCityChange(null)
                            showCityMenu = false
                        }
                    )
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                onCityChange(city)
                                showCityMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Ordenación
        item {
            Box {
                FilterChip(
                    selected = true,
                    onClick = { showSortMenu = true },
                    label = { Text(sortOption.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onSortChange(option)
                                showSortMenu = false
                            },
                            trailingIcon = if (sortOption == option) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }
        }

        // Botón limpiar filtros
        if (hasActiveFilters) {
            item {
                AssistChip(
                    onClick = onClearFilters,
                    label = { Text("Limpiar") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Opciones de ordenación
 */
enum class SortOption(val label: String) {
    NAME("Nombre"),
    DURATION("Duración"),
    DISTANCE("Distancia"),
    STOPS("Paradas")
}
