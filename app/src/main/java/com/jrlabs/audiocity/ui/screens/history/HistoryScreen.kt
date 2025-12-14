package com.jrlabs.audiocity.ui.screens.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.audiocity.domain.model.HistoryGroup
import com.jrlabs.audiocity.domain.model.RouteHistoryEntry
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.theme.ACWarning
import com.jrlabs.audiocity.ui.viewmodel.HistoryScreenStats
import com.jrlabs.audiocity.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyGrouped by viewModel.historyGrouped.collectAsState()
    val stats by viewModel.stats.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Borrar historial") },
            text = { Text("Se eliminarán todos los registros del historial. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial", fontWeight = FontWeight.Bold) },
                actions = {
                    if (historyGrouped.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Más opciones"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Borrar historial") },
                                    onClick = {
                                        showMenu = false
                                        showClearDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (historyGrouped.isEmpty()) {
            EmptyHistoryView(modifier = Modifier.padding(paddingValues))
        } else {
            HistoryListView(
                historyGrouped = historyGrouped,
                stats = stats,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyHistoryView(modifier: Modifier = Modifier) {
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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = ACPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sin historial",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aquí aparecerán las rutas que completes",
            style = MaterialTheme.typography.bodyMedium,
            color = ACTextSecondary
        )
    }
}

@Composable
private fun HistoryListView(
    historyGrouped: List<HistoryGroup>,
    stats: HistoryScreenStats,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats section
        item {
            HistoryStatsCard(stats = stats)
        }

        // History grouped by date
        historyGrouped.forEach { group ->
            item {
                Text(
                    text = group.displayDate,
                    style = MaterialTheme.typography.titleSmall,
                    color = ACTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(group.entries, key = { it.id }) { record ->
                HistoryRecordCard(
                    record = record,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HistoryStatsCard(stats: HistoryScreenStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HistoryStatItem(
                icon = Icons.Default.Map,
                value = "${stats.totalRoutes}",
                label = "Rutas",
                color = ACPrimary
            )

            HistoryStatItem(
                icon = Icons.Default.DirectionsWalk,
                value = stats.totalDistanceFormatted,
                label = "Recorrido",
                color = ACSuccess
            )

            HistoryStatItem(
                icon = Icons.Default.Timer,
                value = stats.totalDurationFormatted,
                label = "Tiempo",
                color = Color(0xFF2196F3)
            )

            HistoryStatItem(
                icon = Icons.Default.CheckCircle,
                value = "${stats.completionRate}%",
                label = "Completado",
                color = ACSuccess
            )
        }
    }
}

@Composable
private fun HistoryStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
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
            color = ACTextSecondary
        )
    }
}

@Composable
private fun HistoryRecordCard(
    record: RouteHistoryEntry,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = record.completionPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    val progressColor = if (record.isFullyCompleted) ACSuccess else ACWarning
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeFormatted = timeFormatter.format(record.completedAt)
    val durationFormatted = if (record.durationMinutes >= 60) {
        "${record.durationMinutes / 60}h ${record.durationMinutes % 60}m"
    } else {
        "${record.durationMinutes}m"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Circle
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${record.completionPercentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Route Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.routeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (record.isFullyCompleted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completado",
                            tint = ACSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = record.routeCity,
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Meta badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetaBadge(icon = Icons.Default.Schedule, text = timeFormatted)
                    MetaBadge(icon = Icons.Default.Timer, text = durationFormatted)
                    MetaBadge(
                        icon = Icons.Default.Map,
                        text = "${record.stopsVisited}/${record.totalStops}"
                    )
                }
            }
        }
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
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = ACTextSecondary
        )
    }
}
