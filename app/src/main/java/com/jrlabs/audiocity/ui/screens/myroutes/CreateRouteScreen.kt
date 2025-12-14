package com.jrlabs.audiocity.ui.screens.myroutes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.audiocity.ui.theme.ACInfo
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.viewmodel.UserRoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouteScreen(
    modifier: Modifier = Modifier,
    viewModel: UserRoutesViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onRouteCreated: (String) -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && city.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Ruta") },
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
                            val route = viewModel.createRoute(
                                name = name,
                                city = city,
                                description = description,
                                neighborhood = neighborhood
                            )
                            onRouteCreated(route.id)
                        },
                        enabled = isValid
                    ) {
                        Text(
                            text = "Crear",
                            fontWeight = FontWeight.Bold,
                            color = if (isValid) ACPrimary else ACTextSecondary
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Basic Information Section
            Section(title = "Información básica") {
                RouteTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre de la ruta",
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
                    placeholder = "Barrio (opcional)",
                    icon = Icons.Default.Place
                )
            }

            // Description Section
            Section(title = "Descripción") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Describe tu ruta...") }
                )
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ACInfo.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ACInfo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Después de crear la ruta podrás añadir paradas desde el mapa",
                        style = MaterialTheme.typography.bodySmall,
                        color = ACTextSecondary
                    )
                }
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
