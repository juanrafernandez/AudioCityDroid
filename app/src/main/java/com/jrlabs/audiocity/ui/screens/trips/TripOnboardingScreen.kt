package com.jrlabs.audiocity.ui.screens.trips

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Destination
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.ui.components.DifficultyBadge
import com.jrlabs.audiocity.ui.components.PurpleColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de onboarding para planificar un viaje (4 pasos)
 * Equivalente a TripOnboardingView.swift en iOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripOnboardingScreen(
    availableDestinations: List<Destination>,
    availableRoutes: List<Route>,
    onDismiss: () -> Unit,
    onTripCreated: (String, List<String>, Date?, Date?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedDestination by remember { mutableStateOf<Destination?>(null) }
    var selectedRouteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var includeDates by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var downloadOffline by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf<String?>(null) } // "start" or "end"

    // Rutas filtradas por destino seleccionado
    val filteredRoutes = remember(selectedDestination, availableRoutes) {
        selectedDestination?.let { dest ->
            availableRoutes.filter { it.city == dest.city }
        } ?: emptyList()
    }

    val canProceed = when (currentStep) {
        0 -> selectedDestination != null
        1 -> selectedRouteIds.isNotEmpty()
        2 -> true
        3 -> !isCreating
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planificar Viaje") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            )
        },
        bottomBar = {
            // Barra de navegación inferior
            Surface(
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón anterior
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anterior")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Indicador de progreso
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .width(if (index == currentStep) 24.dp else 8.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index <= currentStep) PurpleColor
                                        else PurpleColor.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Botón siguiente/crear
                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                isCreating = true
                                onTripCreated(
                                    selectedDestination!!.city,
                                    selectedRouteIds.toList(),
                                    if (includeDates) startDate else null,
                                    if (includeDates) endDate else null,
                                    downloadOffline
                                )
                            }
                        },
                        enabled = canProceed,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleColor)
                    ) {
                        Text(if (currentStep < 3) "Siguiente" else "Crear Viaje")
                        if (currentStep < 3) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.padding(paddingValues),
            label = "step_transition"
        ) { step ->
            when (step) {
                0 -> DestinationStep(
                    destinations = availableDestinations,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it }
                )
                1 -> RoutesStep(
                    routes = filteredRoutes,
                    selectedRouteIds = selectedRouteIds,
                    onRouteToggle = { routeId ->
                        selectedRouteIds = if (selectedRouteIds.contains(routeId)) {
                            selectedRouteIds - routeId
                        } else {
                            selectedRouteIds + routeId
                        }
                    },
                    onSelectAll = { selectedRouteIds = filteredRoutes.map { it.id }.toSet() },
                    onDeselectAll = { selectedRouteIds = emptySet() }
                )
                2 -> OptionsStep(
                    includeDates = includeDates,
                    onIncludeDatesChange = { includeDates = it },
                    startDate = startDate,
                    endDate = endDate,
                    onStartDateClick = { showDatePicker = "start" },
                    onEndDateClick = { showDatePicker = "end" },
                    downloadOffline = downloadOffline,
                    onDownloadOfflineChange = { downloadOffline = it },
                    estimatedSize = "${selectedRouteIds.size * 60} KB"
                )
                3 -> SummaryStep(
                    destination = selectedDestination!!,
                    routes = filteredRoutes.filter { selectedRouteIds.contains(it.id) },
                    startDate = if (includeDates) startDate else null,
                    endDate = if (includeDates) endDate else null,
                    downloadOffline = downloadOffline,
                    isCreating = isCreating
                )
            }
        }

        // Date picker dialogs
        if (showDatePicker != null) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                if (showDatePicker == "start") {
                                    startDate = date
                                } else {
                                    endDate = date
                                }
                            }
                            showDatePicker = null
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = null }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

/**
 * Paso 1: Selección de destino
 */
@Composable
fun DestinationStep(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onDestinationSelected: (Destination) -> Unit
) {
    val popularDestinations = destinations.filter { it.isPopular }
    val otherDestinations = destinations.filter { !it.isPopular }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "¿A dónde viajas?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona tu destino para ver las rutas disponibles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (popularDestinations.isNotEmpty()) {
            item {
                Text(
                    text = "Destinos populares",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(popularDestinations) { destination ->
                DestinationCard(
                    destination = destination,
                    isSelected = selectedDestination?.id == destination.id,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }

        if (otherDestinations.isNotEmpty()) {
            item {
                Text(
                    text = "Todos los destinos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(otherDestinations) { destination ->
                DestinationCard(
                    destination = destination,
                    isSelected = selectedDestination?.id == destination.id,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

@Composable
fun DestinationCard(
    destination: Destination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurpleColor.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PurpleColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PurpleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = PurpleColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = destination.city,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${destination.routeCount} rutas disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PurpleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Paso 2: Selección de rutas
 */
@Composable
fun RoutesStep(
    routes: List<Route>,
    selectedRouteIds: Set<String>,
    onRouteToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selecciona las rutas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedRouteIds.size} de ${routes.size} seleccionadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                TextButton(
                    onClick = {
                        if (selectedRouteIds.size == routes.size) {
                            onDeselectAll()
                        } else {
                            onSelectAll()
                        }
                    }
                ) {
                    Text(
                        if (selectedRouteIds.size == routes.size) "Deseleccionar todas"
                        else "Seleccionar todas"
                    )
                }
            }
        }

        items(routes) { route ->
            RouteSelectionCard(
                route = route,
                isSelected = selectedRouteIds.contains(route.id),
                onClick = { onRouteToggle(route.id) }
            )
        }
    }
}

@Composable
fun RouteSelectionCard(
    route: Route,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) PurpleColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${route.durationMinutes}m · ${route.numStops} paradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            DifficultyBadge(difficulty = route.difficulty)
        }
    }
}

/**
 * Paso 3: Opciones
 */
@Composable
fun OptionsStep(
    includeDates: Boolean,
    onIncludeDatesChange: (Boolean) -> Unit,
    startDate: Date?,
    endDate: Date?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    downloadOffline: Boolean,
    onDownloadOfflineChange: (Boolean) -> Unit,
    estimatedSize: String
) {
    val dateFormatter = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Opciones del viaje",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Fechas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = PurpleColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Añadir fechas",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Opcional",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = includeDates,
                            onCheckedChange = onIncludeDatesChange
                        )
                    }

                    if (includeDates) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onStartDateClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(startDate?.let { dateFormatter.format(it) } ?: "Fecha inicio")
                            }
                            OutlinedButton(
                                onClick = onEndDateClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(endDate?.let { dateFormatter.format(it) } ?: "Fecha fin")
                            }
                        }
                    }
                }
            }
        }

        // Descarga offline
        item {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = PurpleColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Descargar para uso offline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tamaño estimado: $estimatedSize",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = downloadOffline,
                        onCheckedChange = onDownloadOfflineChange
                    )
                }
            }
        }
    }
}

/**
 * Paso 4: Resumen
 */
@Composable
fun SummaryStep(
    destination: Destination,
    routes: List<Route>,
    startDate: Date?,
    endDate: Date?,
    downloadOffline: Boolean,
    isCreating: Boolean
) {
    val dateFormatter = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Resumen del viaje",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Destino
        item {
            SummaryCard(
                icon = Icons.Default.Place,
                title = "Destino",
                value = destination.city
            )
        }

        // Rutas
        item {
            SummaryCard(
                icon = Icons.Default.Route,
                title = "Rutas seleccionadas",
                value = "${routes.size} rutas"
            )
        }

        items(routes) { route ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${route.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Fechas
        if (startDate != null || endDate != null) {
            item {
                SummaryCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Fechas",
                    value = "${startDate?.let { dateFormatter.format(it) } ?: "Sin fecha"} - ${endDate?.let { dateFormatter.format(it) } ?: "Sin fecha"}"
                )
            }
        }

        // Offline
        item {
            SummaryCard(
                icon = Icons.Default.CloudDone,
                title = "Disponible offline",
                value = if (downloadOffline) "Sí" else "No"
            )
        }

        if (isCreating) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PurpleColor)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurpleColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
