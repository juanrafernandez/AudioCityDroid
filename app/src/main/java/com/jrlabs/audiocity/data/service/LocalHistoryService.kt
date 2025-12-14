package com.jrlabs.audiocity.data.service

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.domain.model.HistoryGroup
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.RouteHistoryEntry
import com.jrlabs.audiocity.domain.service.HistoryService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class StoredHistoryEntry(
    val id: String,
    val routeId: String,
    val routeName: String,
    val routeCity: String,
    val completedAt: Long,
    val durationMinutes: Int,
    val distanceKm: Double,
    val stopsVisited: Int,
    val totalStops: Int,
    val pointsEarned: Int
)

/**
 * Local SharedPreferences implementation of HistoryService.
 */
@Singleton
class LocalHistoryService @Inject constructor(
    @ApplicationContext private val context: Context
) : HistoryService {

    companion object {
        private const val PREFS_NAME = "audiocity_history"
        private const val KEY_HISTORY = "history_entries"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val displayDateFormat = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES"))

    private val _historyEntries = MutableStateFlow<List<RouteHistoryEntry>>(emptyList())
    override val historyEntries: StateFlow<List<RouteHistoryEntry>> = _historyEntries.asStateFlow()

    private val _groupedHistory = MutableStateFlow<List<HistoryGroup>>(emptyList())
    override val groupedHistory: StateFlow<List<HistoryGroup>> = _groupedHistory.asStateFlow()

    private val _totalRoutesCompleted = MutableStateFlow(0)
    override val totalRoutesCompleted: StateFlow<Int> = _totalRoutesCompleted.asStateFlow()

    private val _totalDistanceKm = MutableStateFlow(0.0)
    override val totalDistanceKm: StateFlow<Double> = _totalDistanceKm.asStateFlow()

    private val _totalTimeMinutes = MutableStateFlow(0)
    override val totalTimeMinutes: StateFlow<Int> = _totalTimeMinutes.asStateFlow()

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        try {
            val historyJson = prefs.getString(KEY_HISTORY, null)
            if (historyJson != null) {
                val stored = json.decodeFromString<List<StoredHistoryEntry>>(historyJson)
                _historyEntries.value = stored.map { it.toEntry() }
                updateDerivedValues()
            }
        } catch (e: Exception) {
            _historyEntries.value = emptyList()
        }
    }

    private fun saveToPrefs() {
        try {
            val stored = _historyEntries.value.map { it.toStored() }
            prefs.edit()
                .putString(KEY_HISTORY, json.encodeToString(stored))
                .apply()
        } catch (e: Exception) {
            // Ignore save errors
        }
    }

    private fun updateDerivedValues() {
        val entries = _historyEntries.value

        // Update totals
        _totalRoutesCompleted.value = entries.size
        _totalDistanceKm.value = entries.sumOf { it.distanceKm }
        _totalTimeMinutes.value = entries.sumOf { it.durationMinutes }

        // Group by date
        val grouped = entries
            .sortedByDescending { it.completedAt }
            .groupBy { it.dateKey }
            .map { (dateKey, entriesInGroup) ->
                HistoryGroup(
                    dateKey = dateKey,
                    displayDate = entriesInGroup.firstOrNull()?.let {
                        displayDateFormat.format(it.completedAt)
                    } ?: dateKey,
                    entries = entriesInGroup
                )
            }
        _groupedHistory.value = grouped
    }

    override fun addToHistory(
        route: Route,
        stopsVisited: Int,
        durationMinutes: Int,
        distanceKm: Double,
        pointsEarned: Int
    ) {
        val entry = RouteHistoryEntry(
            routeId = route.id,
            routeName = route.name,
            routeCity = route.city,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            stopsVisited = stopsVisited,
            totalStops = route.numStops,
            pointsEarned = pointsEarned
        )

        _historyEntries.value = listOf(entry) + _historyEntries.value
        updateDerivedValues()
        saveToPrefs()
    }

    override fun clearHistory() {
        _historyEntries.value = emptyList()
        updateDerivedValues()
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    override fun getMostRecentEntry(): RouteHistoryEntry? {
        return _historyEntries.value.maxByOrNull { it.completedAt }
    }

    private fun StoredHistoryEntry.toEntry(): RouteHistoryEntry {
        return RouteHistoryEntry(
            id = id,
            routeId = routeId,
            routeName = routeName,
            routeCity = routeCity,
            completedAt = Date(completedAt),
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            stopsVisited = stopsVisited,
            totalStops = totalStops,
            pointsEarned = pointsEarned
        )
    }

    private fun RouteHistoryEntry.toStored(): StoredHistoryEntry {
        return StoredHistoryEntry(
            id = id,
            routeId = routeId,
            routeName = routeName,
            routeCity = routeCity,
            completedAt = completedAt.time,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            stopsVisited = stopsVisited,
            totalStops = totalStops,
            pointsEarned = pointsEarned
        )
    }
}
