package com.jrlabs.audiocity.data.service

import android.content.Context
import android.content.SharedPreferences
import com.jrlabs.audiocity.domain.model.PointsActionType
import com.jrlabs.audiocity.domain.model.PointsTransaction
import com.jrlabs.audiocity.domain.model.UserLevel
import com.jrlabs.audiocity.domain.model.UserPointsStats
import com.jrlabs.audiocity.domain.service.PointsService
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
private data class StoredPointsStats(
    val totalPoints: Int = 0,
    val completedRoutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalTimeMinutes: Int = 0,
    val dailyBonusDate: String? = null,
    val streakDays: Int = 0,
    val lastActivityDate: String? = null
)

@Serializable
private data class StoredTransaction(
    val id: String,
    val actionType: String,
    val points: Int,
    val description: String,
    val routeId: String?,
    val routeName: String?,
    val timestamp: Long
)

/**
 * Local SharedPreferences implementation of PointsService.
 */
@Singleton
class LocalPointsService @Inject constructor(
    @ApplicationContext private val context: Context
) : PointsService {

    companion object {
        private const val PREFS_NAME = "audiocity_points"
        private const val KEY_STATS = "user_stats"
        private const val KEY_TRANSACTIONS = "transactions"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _stats = MutableStateFlow(UserPointsStats())
    override val stats: StateFlow<UserPointsStats> = _stats.asStateFlow()

    private val _transactions = MutableStateFlow<List<PointsTransaction>>(emptyList())
    override val transactions: StateFlow<List<PointsTransaction>> = _transactions.asStateFlow()

    private val _recentLevelUp = MutableStateFlow<UserLevel?>(null)
    override val recentLevelUp: StateFlow<UserLevel?> = _recentLevelUp.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        try {
            // Load stats
            val statsJson = prefs.getString(KEY_STATS, null)
            if (statsJson != null) {
                val stored = json.decodeFromString<StoredPointsStats>(statsJson)
                _stats.value = UserPointsStats(
                    totalPoints = stored.totalPoints,
                    currentLevel = UserLevel.fromPoints(stored.totalPoints),
                    completedRoutes = stored.completedRoutes,
                    totalDistanceKm = stored.totalDistanceKm,
                    totalTimeMinutes = stored.totalTimeMinutes,
                    dailyBonusDate = stored.dailyBonusDate,
                    streakDays = stored.streakDays,
                    lastActivityDate = stored.lastActivityDate
                )
            }

            // Load transactions
            val transactionsJson = prefs.getString(KEY_TRANSACTIONS, null)
            if (transactionsJson != null) {
                val stored = json.decodeFromString<List<StoredTransaction>>(transactionsJson)
                _transactions.value = stored.map { it.toTransaction() }
            }
        } catch (e: Exception) {
            // Reset on error
            _stats.value = UserPointsStats()
            _transactions.value = emptyList()
        }
    }

    private fun saveToPrefs() {
        try {
            val stored = StoredPointsStats(
                totalPoints = _stats.value.totalPoints,
                completedRoutes = _stats.value.completedRoutes,
                totalDistanceKm = _stats.value.totalDistanceKm,
                totalTimeMinutes = _stats.value.totalTimeMinutes,
                dailyBonusDate = _stats.value.dailyBonusDate,
                streakDays = _stats.value.streakDays,
                lastActivityDate = _stats.value.lastActivityDate
            )
            prefs.edit()
                .putString(KEY_STATS, json.encodeToString(stored))
                .putString(KEY_TRANSACTIONS, json.encodeToString(_transactions.value.map { it.toStored() }))
                .apply()
        } catch (e: Exception) {
            // Ignore save errors
        }
    }

    private fun addPoints(
        actionType: PointsActionType,
        routeId: String? = null,
        routeName: String? = null
    ) {
        val currentLevel = _stats.value.currentLevel
        val newTotal = _stats.value.totalPoints + actionType.points
        val newLevel = UserLevel.fromPoints(newTotal)

        // Create transaction
        val transaction = PointsTransaction(
            actionType = actionType,
            points = actionType.points,
            description = actionType.description,
            routeId = routeId,
            routeName = routeName
        )

        _transactions.value = listOf(transaction) + _transactions.value

        // Update stats
        _stats.value = _stats.value.copy(
            totalPoints = newTotal,
            currentLevel = newLevel,
            lastActivityDate = dateFormat.format(Date())
        )

        // Check for level up
        if (newLevel != currentLevel) {
            _recentLevelUp.value = newLevel
        }

        saveToPrefs()
    }

    override fun awardPointsForCompletingRoute(
        routeId: String,
        routeName: String,
        completionPercentage: Int
    ) {
        if (completionPercentage < 100) return // Only award for full completion

        // Check for daily bonus
        val today = dateFormat.format(Date())
        val lastActivity = _stats.value.lastActivityDate

        if (lastActivity != today) {
            // First route of the day
            addPoints(PointsActionType.FIRST_ROUTE_OF_DAY, routeId, routeName)

            // Check for streaks
            val newStreakDays = if (isConsecutiveDay(lastActivity, today)) {
                _stats.value.streakDays + 1
            } else {
                1
            }

            _stats.value = _stats.value.copy(
                streakDays = newStreakDays,
                dailyBonusDate = today
            )

            // Award streak bonuses
            if (newStreakDays == 3) {
                addPoints(PointsActionType.STREAK_3_DAYS, routeId, routeName)
            } else if (newStreakDays == 7) {
                addPoints(PointsActionType.STREAK_7_DAYS, routeId, routeName)
            }
        }

        // Award completion points
        addPoints(PointsActionType.COMPLETE_ROUTE, routeId, routeName)

        // Update completed routes count
        _stats.value = _stats.value.copy(
            completedRoutes = _stats.value.completedRoutes + 1
        )

        saveToPrefs()
    }

    override fun awardPointsForCreatingRoute(
        routeId: String,
        routeName: String,
        numStops: Int
    ) {
        val actionType = when {
            numStops >= 10 -> PointsActionType.CREATE_LARGE_ROUTE
            numStops >= 5 -> PointsActionType.CREATE_MEDIUM_ROUTE
            numStops >= 3 -> PointsActionType.CREATE_SMALL_ROUTE
            else -> return // Too few stops
        }
        addPoints(actionType, routeId, routeName)
    }

    override fun awardPointsForPublishingRoute(routeId: String, routeName: String) {
        addPoints(PointsActionType.PUBLISH_ROUTE, routeId, routeName)
    }

    override fun awardPointsForRouteUsedByOther(routeId: String, routeName: String) {
        addPoints(PointsActionType.ROUTE_USED_BY_OTHER, routeId, routeName)
    }

    override fun clearLevelUpNotification() {
        _recentLevelUp.value = null
    }

    override fun resetPoints() {
        _stats.value = UserPointsStats()
        _transactions.value = emptyList()
        _recentLevelUp.value = null
        prefs.edit().clear().apply()
    }

    private fun isConsecutiveDay(lastDate: String?, today: String): Boolean {
        if (lastDate == null) return false
        try {
            val last = dateFormat.parse(lastDate) ?: return false
            val todayDate = dateFormat.parse(today) ?: return false
            val diff = todayDate.time - last.time
            val daysDiff = diff / (24 * 60 * 60 * 1000)
            return daysDiff == 1L
        } catch (e: Exception) {
            return false
        }
    }

    private fun StoredTransaction.toTransaction(): PointsTransaction {
        return PointsTransaction(
            id = id,
            actionType = PointsActionType.entries.find { it.name == actionType }
                ?: PointsActionType.COMPLETE_ROUTE,
            points = points,
            description = description,
            routeId = routeId,
            routeName = routeName,
            timestamp = Date(timestamp)
        )
    }

    private fun PointsTransaction.toStored(): StoredTransaction {
        return StoredTransaction(
            id = id,
            actionType = actionType.name,
            points = points,
            description = description,
            routeId = routeId,
            routeName = routeName,
            timestamp = timestamp.time
        )
    }
}
