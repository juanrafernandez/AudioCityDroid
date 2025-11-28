package com.jrlabs.audiocity.ui.screens.profile

import android.Manifest
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.jrlabs.audiocity.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jrlabs.audiocity.ui.theme.BrandBlue
import com.jrlabs.audiocity.ui.theme.Gray500
import com.jrlabs.audiocity.ui.theme.StopVisited
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel
) {
    val context = LocalContext.current
    val currentLocation by viewModel.locationService.currentLocation.collectAsState()

    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header
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
                    color = Gray500
                )
            }
        }

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

        // Debug info
        Text(
            text = "Información de depuración",
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
                currentLocation?.let { location ->
                    Text(
                        text = "Ubicación actual",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray500
                    )
                    Text(
                        text = "Lat: ${String.format("%.6f", location.latitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Lng: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Precisión: ${location.accuracy}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                } ?: run {
                    Text(
                        text = "Ubicación no disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // App version
        Text(
            text = "Versión 1.0.0 (POC)",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
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
            tint = if (isGranted) StopVisited else Gray500,
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
                color = Gray500
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Concedido",
                tint = StopVisited,
                modifier = Modifier.size(24.dp)
            )
        } else {
            TextButton(onClick = onRequest) {
                Text("Conceder")
            }
        }
    }
}
