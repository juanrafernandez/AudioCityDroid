package com.jrlabs.audiocity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.ui.theme.ACInfo
import com.jrlabs.audiocity.ui.theme.ACSecondary
import com.jrlabs.audiocity.ui.theme.ACSecondaryLight
import com.jrlabs.audiocity.ui.theme.ACSuccess
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.theme.ACTextTertiary

/**
 * Card para mostrar un viaje en la sección Mis Viajes
 * Diseño actualizado para coincidir con iOS
 */
@Composable
fun TripCard(
    trip: Trip,
    routeNames: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono turquesa (como iOS)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ACSecondaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ACSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del viaje
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trip.destinationCity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Badge de estado
                    if (trip.isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TripStatusBadge(trip = trip)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Meta badges en fila
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rutas
                    TripMetaBadge(
                        icon = Icons.Default.Map,
                        text = "${trip.routeCount} rutas"
                    )

                    // Offline
                    if (trip.isOfflineAvailable) {
                        TripMetaBadge(
                            icon = Icons.Default.CloudDone,
                            text = "Offline",
                            color = ACSuccess
                        )
                    }

                    // Fechas
                    trip.dateRangeFormatted()?.let { dateRange ->
                        TripMetaBadge(
                            icon = Icons.Default.CalendarMonth,
                            text = dateRange
                        )
                    }
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ACTextTertiary
            )
        }
    }
}

/**
 * Meta badge para información del viaje
 */
@Composable
fun TripMetaBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color = ACTextSecondary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * Badge de estado del viaje
 */
@Composable
fun TripStatusBadge(trip: Trip) {
    val (color, text) = when {
        trip.isCurrent -> Pair(ACSuccess, "Activo")
        trip.isFuture -> Pair(ACInfo, "Próximo")
        trip.isPast -> Pair(Color.Gray, "Pasado")
        else -> Pair(Color.Gray, "")
    }

    if (text.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Card para crear nuevo viaje (estado vacío)
 * Diseño iOS: borde punteado turquesa, icono de avión, chevron derecha
 */
@Composable
fun NewTripCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = ACSecondary.copy(alpha = 0.3f)
    val dashWidth = 6.dp
    val dashGap = 6.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashWidth.toPx(), dashGap.toPx()),
                        0f
                    )
                )
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo turquesa con icono de avión
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ACSecondaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ACSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Planifica tu primer viaje",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Selecciona destino y rutas para tenerlas offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ACTextTertiary
            )
        }
    }
}
