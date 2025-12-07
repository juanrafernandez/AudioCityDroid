package com.jrlabs.audiocity.services

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.data.model.Destination
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Trip
import com.jrlabs.audiocity.data.repository.FirebaseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para gestión de viajes del usuario
 * Equivalente a TripService.swift en iOS
 */
@Singleton
class TripService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseRepository: FirebaseRepository
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trips_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _availableDestinations = MutableStateFlow<List<Destination>>(emptyList())
    val availableDestinations: StateFlow<List<Destination>> = _availableDestinations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadTrips()
    }

    /**
     * IDs de rutas en viajes activos (no pasados)
     * Usado para mostrar pins especiales en el mapa
     */
    val activeRouteIds: Set<String>
        get() = _trips.value
            .filter { !it.isPast }
            .flatMap { it.selectedRouteIds }
            .toSet()

    /**
     * Viaje activo actual (en curso o próximo)
     */
    val activeTrip: Trip?
        get() = _trips.value
            .filter { !it.isPast }
            .sortedBy { it.startDate ?: Date(Long.MAX_VALUE) }
            .firstOrNull()

    /**
     * Viajes próximos/actuales (máximo 2 para mostrar en sección)
     */
    val upcomingTrips: List<Trip>
        get() = _trips.value
            .filter { !it.isPast }
            .sortedBy { it.startDate ?: Date(Long.MAX_VALUE) }
            .take(2)

    /**
     * Crear nuevo viaje
     * @return Trip si se creó exitosamente, null si ya existe un viaje duplicado
     */
    fun createTrip(
        destinationCity: String,
        destinationCountry: String = "España",
        startDate: Date? = null,
        endDate: Date? = null
    ): Trip? {
        // Validar duplicados
        if (tripExists(destinationCity, startDate, endDate)) {
            _errorMessage.value = "Ya existe un viaje a $destinationCity con las mismas fechas"
            return null
        }

        val trip = Trip(
            destinationCity = destinationCity,
            destinationCountry = destinationCountry,
            startDate = startDate,
            endDate = endDate
        )

        val updatedTrips = _trips.value + trip
        _trips.value = updatedTrips
        saveTrips()

        return trip
    }

    /**
     * Añadir ruta a un viaje
     */
    fun addRoute(routeId: String, tripId: String) {
        val updatedTrips = _trips.value.map { trip ->
            if (trip.id == tripId && !trip.selectedRouteIds.contains(routeId)) {
                trip.copy(selectedRouteIds = (trip.selectedRouteIds + routeId).toMutableList())
            } else {
                trip
            }
        }
        _trips.value = updatedTrips
        saveTrips()
    }

    /**
     * Quitar ruta de un viaje
     */
    fun removeRoute(routeId: String, tripId: String) {
        val updatedTrips = _trips.value.map { trip ->
            if (trip.id == tripId) {
                trip.copy(selectedRouteIds = trip.selectedRouteIds.filter { it != routeId }.toMutableList())
            } else {
                trip
            }
        }
        _trips.value = updatedTrips
        saveTrips()
    }

    /**
     * Eliminar viaje
     */
    fun deleteTrip(tripId: String) {
        val updatedTrips = _trips.value.filter { it.id != tripId }
        _trips.value = updatedTrips
        saveTrips()
    }

    /**
     * Actualizar fechas de un viaje
     */
    fun updateTripDates(tripId: String, startDate: Date?, endDate: Date?) {
        val updatedTrips = _trips.value.map { trip ->
            if (trip.id == tripId) {
                trip.copy(startDate = startDate, endDate = endDate)
            } else {
                trip
            }
        }
        _trips.value = updatedTrips
        saveTrips()
    }

    /**
     * Marcar viaje como disponible offline
     */
    fun markAsOfflineAvailable(tripId: String, available: Boolean) {
        val updatedTrips = _trips.value.map { trip ->
            if (trip.id == tripId) {
                trip.copy(
                    isOfflineAvailable = available,
                    lastSyncDate = if (available) Date() else null
                )
            } else {
                trip
            }
        }
        _trips.value = updatedTrips
        saveTrips()
    }

    /**
     * Obtener viajes por ciudad
     */
    fun getTrips(forCity: String): List<Trip> {
        return _trips.value.filter { it.destinationCity == forCity }
    }

    /**
     * Obtener viaje por ID
     */
    fun getTrip(byId: String): Trip? {
        return _trips.value.find { it.id == byId }
    }

    /**
     * Verificar si existe viaje duplicado
     */
    fun tripExists(city: String, startDate: Date?, endDate: Date?): Boolean {
        return _trips.value.any { trip ->
            trip.destinationCity == city &&
            isSameDay(trip.startDate, startDate) &&
            isSameDay(trip.endDate, endDate)
        }
    }

    /**
     * Cargar destinos disponibles desde Firebase
     */
    suspend fun loadAvailableDestinations(routes: List<Route>) {
        _isLoading.value = true

        try {
            // Agrupar rutas por ciudad
            val routesByCity = routes.groupBy { it.city }

            val destinations = routesByCity.map { (city, cityRoutes) ->
                Destination(
                    city = city,
                    country = "España",
                    routeCount = cityRoutes.size,
                    isPopular = cityRoutes.size >= 3
                )
            }.sortedByDescending { it.isPopular }

            _availableDestinations.value = destinations
        } catch (e: Exception) {
            _errorMessage.value = "Error cargando destinos: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    // MARK: - Persistencia

    private fun loadTrips() {
        val tripsJson = prefs.getString("user_trips", null)
        if (tripsJson != null) {
            try {
                val tripDataList = json.decodeFromString<List<TripData>>(tripsJson)
                _trips.value = tripDataList.map { it.toTrip() }
            } catch (e: Exception) {
                _trips.value = emptyList()
            }
        }
    }

    private fun saveTrips() {
        try {
            val tripDataList = _trips.value.map { TripData.fromTrip(it) }
            val tripsJson = json.encodeToString(tripDataList)
            prefs.edit().putString("user_trips", tripsJson).apply()
        } catch (e: Exception) {
            _errorMessage.value = "Error guardando viajes: ${e.message}"
        }
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null && date2 == null) return true
        if (date1 == null || date2 == null) return false

        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

/**
 * Clase auxiliar para serialización de Trip
 */
@kotlinx.serialization.Serializable
private data class TripData(
    val id: String,
    val destinationCity: String,
    val destinationCountry: String,
    val selectedRouteIds: List<String>,
    val createdAt: Long,
    val startDate: Long?,
    val endDate: Long?,
    val isOfflineAvailable: Boolean,
    val lastSyncDate: Long?
) {
    fun toTrip(): Trip {
        return Trip(
            id = id,
            destinationCity = destinationCity,
            destinationCountry = destinationCountry,
            selectedRouteIds = selectedRouteIds.toMutableList(),
            createdAt = Date(createdAt),
            startDate = startDate?.let { Date(it) },
            endDate = endDate?.let { Date(it) },
            isOfflineAvailable = isOfflineAvailable,
            lastSyncDate = lastSyncDate?.let { Date(it) }
        )
    }

    companion object {
        fun fromTrip(trip: Trip): TripData {
            return TripData(
                id = trip.id,
                destinationCity = trip.destinationCity,
                destinationCountry = trip.destinationCountry,
                selectedRouteIds = trip.selectedRouteIds,
                createdAt = trip.createdAt.time,
                startDate = trip.startDate?.time,
                endDate = trip.endDate?.time,
                isOfflineAvailable = trip.isOfflineAvailable,
                lastSyncDate = trip.lastSyncDate?.time
            )
        }
    }
}
