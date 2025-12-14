package com.jrlabs.audiocity.domain.model

import java.util.Date
import java.util.UUID

/**
 * Represents a user's level in the gamification system.
 * Matches iOS implementation.
 */
enum class UserLevel(
    val displayName: String,
    val iconName: String,
    val minPoints: Int,
    val maxPoints: Int
) {
    EXPLORER("Explorador", "directions_walk", 0, 99),
    TRAVELER("Viajero", "flight", 100, 299),
    LOCAL_GUIDE("Guía Local", "map", 300, 599),
    EXPERT("Experto", "star", 600, 999),
    MASTER("Maestro AudioCity", "emoji_events", 1000, Int.MAX_VALUE);

    companion object {
        fun fromPoints(points: Int): UserLevel {
            return entries.find { points in it.minPoints..it.maxPoints } ?: EXPLORER
        }
    }

    /**
     * Returns the progress percentage towards the next level.
     */
    fun progressToNext(currentPoints: Int): Float {
        if (this == MASTER) return 1f
        val pointsInLevel = currentPoints - minPoints
        val levelRange = maxPoints - minPoints + 1
        return (pointsInLevel.toFloat() / levelRange).coerceIn(0f, 1f)
    }

    /**
     * Returns points needed for next level.
     */
    fun pointsToNextLevel(currentPoints: Int): Int {
        if (this == MASTER) return 0
        return maxPoints + 1 - currentPoints
    }
}

/**
 * Represents user's points statistics.
 */
data class UserPointsStats(
    val totalPoints: Int = 0,
    val currentLevel: UserLevel = UserLevel.EXPLORER,
    val completedRoutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalTimeMinutes: Int = 0,
    val dailyBonusDate: String? = null,
    val streakDays: Int = 0,
    val lastActivityDate: String? = null
) {
    val progressToNextLevel: Float
        get() = currentLevel.progressToNext(totalPoints)

    val pointsToNextLevel: Int
        get() = currentLevel.pointsToNextLevel(totalPoints)
}

/**
 * Types of actions that award points.
 */
enum class PointsActionType(val points: Int, val description: String) {
    COMPLETE_ROUTE(30, "Completar ruta al 100%"),
    FIRST_ROUTE_OF_DAY(10, "Primera ruta del día"),
    STREAK_3_DAYS(50, "Racha de 3 días"),
    STREAK_7_DAYS(100, "Racha de 7 días"),
    CREATE_SMALL_ROUTE(50, "Crear ruta pequeña (3-4 paradas)"),
    CREATE_MEDIUM_ROUTE(100, "Crear ruta mediana (5-9 paradas)"),
    CREATE_LARGE_ROUTE(200, "Crear ruta extensa (10+ paradas)"),
    PUBLISH_ROUTE(20, "Publicar ruta"),
    ROUTE_USED_BY_OTHER(5, "Tu ruta usada por otros")
}

/**
 * Represents a single points transaction.
 */
data class PointsTransaction(
    val id: String = UUID.randomUUID().toString(),
    val actionType: PointsActionType,
    val points: Int,
    val description: String,
    val routeId: String? = null,
    val routeName: String? = null,
    val timestamp: Date = Date()
)
