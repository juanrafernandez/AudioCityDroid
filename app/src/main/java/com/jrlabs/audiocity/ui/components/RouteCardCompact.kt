package com.jrlabs.audiocity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrlabs.audiocity.data.model.Route

/**
 * Card compacta para mostrar rutas en carruseles horizontales
 * Equivalente a RouteCardCompact en iOS (180px width)
 */
@Composable
fun RouteCardCompact(
    route: Route,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Icono de categoría + Badge de dificultad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de categoría
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getCategoryColor(route.neighborhood).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(route.neighborhood),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = getCategoryColor(route.neighborhood)
                    )
                }

                // Badge de dificultad
                DifficultyBadge(difficulty = route.difficulty)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de la ruta (2 líneas máximo, altura fija)
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ubicación
            Text(
                text = "${route.city}, ${route.neighborhood}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats: Duración + Número de paradas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duración
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${route.durationMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Número de paradas
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${route.numStops}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Badge de dificultad
 */
@Composable
fun DifficultyBadge(difficulty: String) {
    val (color, text) = when (difficulty.lowercase()) {
        "fácil", "facil", "easy" -> Pair(Color(0xFF4CAF50), "Fácil")
        "media", "medium" -> Pair(Color(0xFFFF9800), "Media")
        "difícil", "dificil", "hard" -> Pair(Color(0xFFF44336), "Difícil")
        else -> Pair(Color.Gray, difficulty)
    }

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

/**
 * Obtener icono según el barrio/categoría
 */
fun getCategoryIcon(neighborhood: String): ImageVector {
    return when (neighborhood.lowercase()) {
        "arganzuela" -> Icons.Default.Business
        "centro" -> Icons.Default.MenuBook
        "chamberí", "chamberi" -> Icons.Default.Water
        "lavapiés", "lavapies" -> Icons.Default.Restaurant
        else -> Icons.Default.Map
    }
}

/**
 * Obtener color según el barrio/categoría
 */
fun getCategoryColor(neighborhood: String): Color {
    return when (neighborhood.lowercase()) {
        "arganzuela" -> Color(0xFFFF9800) // Orange
        "centro" -> Color(0xFF9C27B0) // Purple
        "chamberí", "chamberi" -> Color(0xFF00BCD4) // Cyan
        "lavapiés", "lavapies" -> Color(0xFFF44336) // Red
        else -> Color(0xFF2196F3) // Blue
    }
}
