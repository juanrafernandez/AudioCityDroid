package com.jrlabs.audiocity.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.jrlabs.audiocity.domain.model.HistoryGroup
import com.jrlabs.audiocity.domain.model.RouteHistoryEntry
import com.jrlabs.audiocity.domain.service.HistoryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HistoryScreenStats(
    val totalRoutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationMinutes: Int = 0,
    val completionRate: Int = 0
) {
    val totalDistanceFormatted: String
        get() = if (totalDistanceKm >= 1.0) {
            String.format("%.1f km", totalDistanceKm)
        } else {
            "${(totalDistanceKm * 1000).toInt()} m"
        }

    val totalDurationFormatted: String
        get() {
            val hours = totalDurationMinutes / 60
            val minutes = totalDurationMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyService: HistoryService
) : ViewModel() {

    val history: StateFlow<List<RouteHistoryEntry>> = historyService.historyEntries

    val historyGrouped: StateFlow<List<HistoryGroup>> = historyService.groupedHistory

    private val _stats = MutableStateFlow(HistoryScreenStats())
    val stats: StateFlow<HistoryScreenStats> = _stats.asStateFlow()

    init {
        updateStats()
    }

    private fun updateStats() {
        val historyList = history.value
        if (historyList.isEmpty()) {
            _stats.value = HistoryScreenStats()
            return
        }

        val totalRoutes = historyList.size
        val totalDistance = historyList.sumOf { it.distanceKm }
        val totalDuration = historyList.sumOf { it.durationMinutes }
        val completedCount = historyList.count { it.isFullyCompleted }
        val completionRate = if (totalRoutes > 0) (completedCount * 100) / totalRoutes else 0

        _stats.value = HistoryScreenStats(
            totalRoutes = totalRoutes,
            totalDistanceKm = totalDistance,
            totalDurationMinutes = totalDuration,
            completionRate = completionRate
        )
    }

    fun clearHistory() {
        historyService.clearHistory()
        updateStats()
    }
}
