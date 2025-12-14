package com.jrlabs.audiocity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACPrimaryDark
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.theme.ACTextTertiary

/**
 * Card compacta para mostrar rutas en carruseles horizontales
 * Equivalente a ACCompactRouteCard en iOS (180px width)
 * Diseño: imagen de cabecera con gradiente + info debajo
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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header con imagen o gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                // Imagen de fondo o gradiente por defecto
                if (route.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(route.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = route.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    )
                    // Gradiente oscuro para legibilidad del badge
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                } else {
                    // Fondo con gradiente coral (como iOS)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ACPrimary.copy(alpha = 0.8f),
                                        ACPrimaryDark
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icono decorativo centrado
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                // Badge de paradas (esquina inferior izquierda)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "${route.numStops} paradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Info debajo de la imagen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
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

                // Ciudad y duración en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = route.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${route.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextTertiary
                    )
                }
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
