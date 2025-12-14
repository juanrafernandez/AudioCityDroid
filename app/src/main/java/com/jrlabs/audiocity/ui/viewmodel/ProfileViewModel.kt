package com.jrlabs.audiocity.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.jrlabs.audiocity.domain.model.UserLevel
import com.jrlabs.audiocity.domain.model.UserPointsStats
import com.jrlabs.audiocity.domain.service.HistoryService
import com.jrlabs.audiocity.domain.service.PointsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileHistoryStats(
    val completedRoutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalTimeMinutes: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val pointsService: PointsService,
    private val historyService: HistoryService
) : ViewModel() {

    val stats: StateFlow<UserPointsStats> = pointsService.stats

    val recentLevelUp: StateFlow<UserLevel?> = pointsService.recentLevelUp

    private val _historyStats = MutableStateFlow(ProfileHistoryStats())
    val historyStats: StateFlow<ProfileHistoryStats> = _historyStats.asStateFlow()

    init {
        // Combine history stats
        updateHistoryStats()
    }

    private fun updateHistoryStats() {
        _historyStats.value = ProfileHistoryStats(
            completedRoutes = historyService.totalRoutesCompleted.value,
            totalDistanceKm = historyService.totalDistanceKm.value,
            totalTimeMinutes = historyService.totalTimeMinutes.value
        )
    }

    fun clearLevelUpNotification() {
        pointsService.clearLevelUpNotification()
    }
}
