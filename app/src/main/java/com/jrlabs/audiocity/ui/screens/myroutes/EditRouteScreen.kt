package com.jrlabs.audiocity.ui.screens.myroutes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.audiocity.domain.model.UserRoute
import com.jrlabs.audiocity.domain.model.UserStop
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.viewmodel.UserRoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRouteScreen(
    routeId: String,
    modifier: Modifier = Modifier,
    viewModel: UserRoutesViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onAddStop: (String) -> Unit = {}
) {
    val route = viewModel.getRoute(routeId)

    if (route == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var name by remember { mutableStateOf(route.name) }
    var city by remember { mutableStateOf(route.city) }
    var neighborhood by remember { mutableStateOf(route.neighborhood) }
    var description by remember { mutableStateOf(route.description) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ruta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta ruta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoute(routeId)
                        showDeleteDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Ruta") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val updatedRoute = route.copy(
                                name = name,
                                city = city,
                                neighborhood = neighborhood,
                                description = description
                            )
                            viewModel.updateRoute(updatedRoute)
                            onDismiss()
                        },
                        enabled = name.isNotBlank() && city.isNotBlank()
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
                            color = if (name.isNotBlank() && city.isNotBlank()) ACPrimary else ACTextSecondary
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Basic Information Section
            item {
                Section(title = "Información") {
                    RouteTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Nombre",
                        icon = Icons.Default.Map
                    )

                    RouteTextField(
                        value = city,
                        onValueChange = { city = it },
                        placeholder = "Ciudad",
                        icon = Icons.Default.LocationCity
                    )

                    RouteTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        placeholder = "Barrio",
                        icon = Icons.Default.Place
                    )
                }
            }

            // Description Section
            item {
                Section(title = "Descripción") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Describe tu ruta...") }
                    )
                }
            }

            // Stops Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paradas (${route.stops.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = ACTextSecondary
                    )

                    TextButton(onClick = { onAddStop(routeId) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }
            }

            if (route.stops.isEmpty()) {
                item {
                    EmptyStopsCard(onAddStop = { onAddStop(routeId) })
                }
            } else {
                itemsIndexed(
                    route.stops.sortedBy { it.order },
                    key = { _, stop -> stop.id }
                ) { _, stop ->
                    StopEditCard(stop = stop)
                }
            }

            // Publish Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PublishCard(
                    isPublished = route.isPublished,
                    onToggle = { viewModel.togglePublish(routeId) }
                )
            }

            // Delete Button
            item {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Ruta")
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = ACTextSecondary
        )
        content()
    }
}

@Composable
private fun RouteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ACTextSecondary
            )
        },
        singleLine = true
    )
}

@Composable
private fun EmptyStopsCard(onAddStop: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddStop),
        border = BorderStroke(
            width = 1.5.dp,
            color = ACPrimary.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ACPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = ACPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Añadir primera parada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Define los puntos de interés de tu ruta",
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ACTextSecondary
            )
        }
    }
}

@Composable
private fun StopEditCard(stop: UserStop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ACPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stop.order}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (stop.description.isNotEmpty()) {
                    Text(
                        text = stop.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reordenar",
                tint = ACTextSecondary
            )
        }
    }
}

@Composable
private fun PublishCard(
    isPublished: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = "Publicar ruta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Las rutas publicadas serán visibles para otros usuarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )
            }

            Switch(
                checked = isPublished,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ACPrimary
                )
            )
        }
    }
}
