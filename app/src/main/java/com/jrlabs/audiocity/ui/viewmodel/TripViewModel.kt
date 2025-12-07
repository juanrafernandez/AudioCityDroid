package com.jrlabs.audiocity.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jrlabs.audiocity.data.model.Destination
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.data.repository.FirebaseRepository
import com.jrlabs.audiocity.services.TripService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel para gestión de viajes
 */
@HiltViewModel
class TripViewModel @Inject constructor(
    application: Application,
    private val tripService: TripService,
    private val firebaseRepository: FirebaseRepository
) : AndroidViewModel(application) {

    // Exponer trips desde el servicio
    val trips: StateFlow<List<Trip>> = tripService.trips
    val availableDestinations: StateFlow<List<Destination>> = tripService.availableDestinations
    val isLoading: StateFlow<Boolean> = tripService.isLoading
    val errorMessage: StateFlow<String?> = tripService.errorMessage

    init {
        loadDestinations()
    }

    /**
     * Cargar destinos disponibles desde Firebase
     */
    private fun loadDestinations() {
        viewModelScope.launch {
            val result = firebaseRepository.fetchAllRoutes()
            result.onSuccess { routes ->
                tripService.loadAvailableDestinations(routes)
            }
        }
    }

    /**
     * Crear nuevo viaje
     */
    fun createTrip(
        city: String,
        routeIds: List<String>,
        startDate: Date?,
        endDate: Date?,
        downloadOffline: Boolean
    ) {
        val trip = tripService.createTrip(
            destinationCity = city,
            startDate = startDate,
            endDate = endDate
        )

        trip?.let { newTrip ->
            // Añadir rutas seleccionadas
            routeIds.forEach { routeId ->
                tripService.addRoute(routeId, newTrip.id)
            }

            // Marcar como offline si se solicitó
            if (downloadOffline) {
                tripService.markAsOfflineAvailable(newTrip.id, true)
            }
        }
    }

    /**
     * Obtener viaje por ID
     */
    fun getTrip(tripId: String): Trip? {
        return tripService.getTrip(tripId)
    }

    /**
     * Eliminar viaje
     */
    fun deleteTrip(tripId: String) {
        tripService.deleteTrip(tripId)
    }

    /**
     * Añadir ruta a viaje
     */
    fun addRoute(routeId: String, tripId: String) {
        tripService.addRoute(routeId, tripId)
    }

    /**
     * Quitar ruta de viaje
     */
    fun removeRoute(routeId: String, tripId: String) {
        tripService.removeRoute(routeId, tripId)
    }

    /**
     * Actualizar fechas del viaje
     */
    fun updateTripDates(tripId: String, startDate: Date?, endDate: Date?) {
        tripService.updateTripDates(tripId, startDate, endDate)
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        tripService.clearError()
    }
}
