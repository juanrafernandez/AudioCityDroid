package com.jrlabs.audiocity.ui.screens.profile

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jrlabs.audiocity.R
import com.jrlabs.audiocity.domain.model.UserLevel
import com.jrlabs.audiocity.ui.theme.ACGold
import com.jrlabs.audiocity.ui.theme.ACInfo
import com.jrlabs.audiocity.ui.theme.ACLevelExpert
import com.jrlabs.audiocity.ui.theme.ACLevelExplorer
import com.jrlabs.audiocity.ui.theme.ACLevelLocalGuide
import com.jrlabs.audiocity.ui.theme.ACLevelMaster
import com.jrlabs.audiocity.ui.theme.ACLevelTraveler
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.theme.ACWarning
import com.jrlabs.audiocity.ui.viewmodel.ProfileViewModel

// Level colors - Basados en Theme.swift iOS
val LevelExplorer = ACLevelExplorer    // Gris - Inicio
val LevelTraveler = ACLevelTraveler    // Azul - Progreso
val LevelGuide = ACLevelLocalGuide     // Verde - Intermedio
val LevelExpert = ACLevelExpert        // Púrpura - Avanzado
val LevelMaster = ACLevelMaster        // Coral - Maestro

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val historyStats by viewModel.historyStats.collectAsState()
    val recentLevelUp by viewModel.recentLevelUp.collectAsState()

    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val scrollState = rememberScrollState()

    // Level up notification
    recentLevelUp?.let { newLevel ->
        LevelUpDialog(
            level = newLevel,
            onDismiss = { viewModel.clearLevelUpNotification() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App header with logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_app_transp),
                contentDescription = "AudioCity Logo",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AudioCity",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Descubre tu ciudad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ACTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Points and Level Section
        PointsLevelCard(
            level = stats.currentLevel,
            totalPoints = stats.totalPoints,
            progress = stats.progressToNextLevel,
            pointsToNext = stats.pointsToNextLevel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics Section
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatsGrid(
            completedRoutes = historyStats.completedRoutes,
            totalDistanceKm = historyStats.totalDistanceKm,
            totalTimeMinutes = historyStats.totalTimeMinutes,
            streakDays = stats.streakDays
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permissions section
        Text(
            text = "Permisos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                PermissionItem(
                    icon = Icons.Default.LocationOn,
                    title = "Ubicación",
                    description = "Necesario para detectar paradas",
                    isGranted = fineLocationPermission.status.isGranted,
                    onRequest = { fineLocationPermission.launchPermissionRequest() }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                PermissionItem(
                    icon = Icons.Default.MyLocation,
                    title = "Ubicación en segundo plano",
                    description = "Para seguir la ruta con la app cerrada",
                    isGranted = backgroundLocationPermission?.status?.isGranted ?: true,
                    onRequest = { backgroundLocationPermission?.launchPermissionRequest() }
                )

                if (notificationPermission != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    PermissionItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        description = "Para mostrar el estado de la ruta",
                        isGranted = notificationPermission.status.isGranted,
                        onRequest = { notificationPermission.launchPermissionRequest() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App version
        Text(
            text = "Versión 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = ACTextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PointsLevelCard(
    level: UserLevel,
    totalPoints: Int,
    progress: Float,
    pointsToNext: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    val levelColor = when (level) {
        UserLevel.EXPLORER -> LevelExplorer
        UserLevel.TRAVELER -> LevelTraveler
        UserLevel.LOCAL_GUIDE -> LevelGuide
        UserLevel.EXPERT -> LevelExpert
        UserLevel.MASTER -> LevelMaster
    }

    val levelIcon = when (level) {
        UserLevel.EXPLORER -> Icons.Default.DirectionsWalk
        UserLevel.TRAVELER -> Icons.Default.Flight
        UserLevel.LOCAL_GUIDE -> Icons.Default.Map
        UserLevel.EXPERT -> Icons.Default.Star
        UserLevel.MASTER -> Icons.Default.EmojiEvents
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = levelColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(levelColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = levelIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level name
            Text(
                text = level.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = levelColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Total points
            Text(
                text = "$totalPoints puntos",
                style = MaterialTheme.typography.titleMedium,
                color = ACTextSecondary
            )

            if (level != UserLevel.MASTER) {
                Spacer(modifier = Modifier.height(16.dp))

                // Progress to next level
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = levelColor,
                        trackColor = levelColor.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$pointsToNext puntos para el siguiente nivel",
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextSecondary
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Nivel máximo alcanzado!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = levelColor
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(
    completedRoutes: Int,
    totalDistanceKm: Double,
    totalTimeMinutes: Int,
    streakDays: Int
) {
    val hours = totalTimeMinutes / 60
    val minutes = totalTimeMinutes % 60
    val timeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Route,
            value = completedRoutes.toString(),
            label = "Rutas",
            color = ACPrimary
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DirectionsWalk,
            value = String.format("%.1f km", totalDistanceKm),
            label = "Distancia",
            color = ACSuccess
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Timer,
            value = timeString,
            label = "Tiempo",
            color = Color(0xFFFF9800)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Star,
            value = "$streakDays días",
            label = "Racha",
            color = LevelExpert
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = ACTextSecondary
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) ACSuccess else ACTextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ACTextSecondary
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Concedido",
                tint = ACSuccess,
                modifier = Modifier.size(24.dp)
            )
        } else {
            TextButton(onClick = onRequest) {
                Text("Conceder")
            }
        }
    }
}

@Composable
private fun LevelUpDialog(
    level: UserLevel,
    onDismiss: () -> Unit
) {
    val levelColor = when (level) {
        UserLevel.EXPLORER -> LevelExplorer
        UserLevel.TRAVELER -> LevelTraveler
        UserLevel.LOCAL_GUIDE -> LevelGuide
        UserLevel.EXPERT -> LevelExpert
        UserLevel.MASTER -> LevelMaster
    }

    val levelIcon = when (level) {
        UserLevel.EXPLORER -> Icons.Default.DirectionsWalk
        UserLevel.TRAVELER -> Icons.Default.Flight
        UserLevel.LOCAL_GUIDE -> Icons.Default.Map
        UserLevel.EXPERT -> Icons.Default.Star
        UserLevel.MASTER -> Icons.Default.EmojiEvents
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Nuevo nivel!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(levelColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = levelIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = level.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onDismiss) {
                    Text("¡Genial!")
                }
            }
        }
    }
}
