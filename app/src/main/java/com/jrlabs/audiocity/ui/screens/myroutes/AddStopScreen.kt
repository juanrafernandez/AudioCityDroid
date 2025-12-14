package com.jrlabs.audiocity.ui.screens.myroutes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jrlabs.audiocity.domain.model.UserStop
import com.jrlabs.audiocity.ui.theme.ACPrimary
import com.jrlabs.audiocity.ui.theme.ACTextSecondary
import com.jrlabs.audiocity.ui.viewmodel.UserRoutesViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopScreen(
    routeId: String,
    modifier: Modifier = Modifier,
    viewModel: UserRoutesViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var scriptEs by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() &&
            latitude.toDoubleOrNull() != null &&
            longitude.toDoubleOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Parada") },
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
                            val lat = latitude.toDoubleOrNull() ?: return@TextButton
                            val lon = longitude.toDoubleOrNull() ?: return@TextButton

                            val stop = UserStop(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                latitude = lat,
                                longitude = lon,
                                scriptEs = scriptEs,
                                order = 0 // Will be set by the service
                            )

                            viewModel.addStop(routeId, stop)
                            onDismiss()
                        },
                        enabled = isValid
                    ) {
                        Text(
                            text = "Añadir",
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
            // Stop Information Section
            Section(title = "Información de la parada") {
                StopTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre",
                    icon = Icons.Default.Place
                )

                StopTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Descripción breve",
                    icon = Icons.Default.Description
                )
            }

            // Location Section
            Section(title = "Ubicación") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Latitud") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ACTextSecondary
                            )
                        },
                        singleLine = true,
                        isError = latitude.isNotEmpty() && latitude.toDoubleOrNull() == null
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Longitud") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ACTextSecondary
                            )
                        },
                        singleLine = true,
                        isError = longitude.isNotEmpty() && longitude.toDoubleOrNull() == null
                    )
                }
            }

            // Narration Section
            Section(title = "Narración") {
                OutlinedTextField(
                    value = scriptEs,
                    onValueChange = { scriptEs = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Escribe el texto de la narración...") }
                )

                Text(
                    text = "Escribe el texto que se reproducirá cuando el usuario llegue a esta parada",
                    style = MaterialTheme.typography.bodySmall,
                    color = ACTextSecondary
                )
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
private fun StopTextField(
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
