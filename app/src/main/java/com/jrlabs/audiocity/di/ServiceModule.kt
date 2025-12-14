package com.jrlabs.audiocity.di

import com.jrlabs.audiocity.data.service.DefaultRouteOptimizationService
import com.jrlabs.audiocity.data.service.FusedLocationService
import com.jrlabs.audiocity.data.service.LocalFavoritesService
import com.jrlabs.audiocity.data.service.LocalHistoryService
import com.jrlabs.audiocity.data.service.LocalPointsService
import com.jrlabs.audiocity.data.service.LocalUserRoutesService
import com.jrlabs.audiocity.data.service.TextToSpeechAudioPreviewService
import com.jrlabs.audiocity.data.service.TextToSpeechAudioService
import com.jrlabs.audiocity.domain.service.AudioPreviewService
import com.jrlabs.audiocity.domain.service.AudioService
import com.jrlabs.audiocity.domain.service.FavoritesService
import com.jrlabs.audiocity.domain.service.HistoryService
import com.jrlabs.audiocity.domain.service.LocationService
import com.jrlabs.audiocity.domain.service.PointsService
import com.jrlabs.audiocity.domain.service.RouteOptimizationService
import com.jrlabs.audiocity.domain.service.UserRoutesService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding Service interfaces to implementations.
 *
 * Follows Interface Segregation Principle:
 * - Each service interface has a specific responsibility
 * - Clients depend only on the interfaces they need
 *
 * Follows Open/Closed Principle:
 * - New implementations can be added without modifying existing code
 * - Just create a new implementation and bind it here
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    // ============ AUDIO SERVICES ============

    /**
     * Binds TextToSpeechAudioService to AudioService interface.
     * Used for active route audio playback.
     */
    @Binds
    @Singleton
    abstract fun bindAudioService(
        textToSpeechAudioService: TextToSpeechAudioService
    ): AudioService

    /**
     * Binds TextToSpeechAudioPreviewService to AudioPreviewService interface.
     * Used for stop preview audio in route detail view.
     */
    @Binds
    @Singleton
    abstract fun bindAudioPreviewService(
        textToSpeechAudioPreviewService: TextToSpeechAudioPreviewService
    ): AudioPreviewService

    // ============ LOCATION SERVICES ============

    /**
     * Binds FusedLocationService to LocationService interface.
     */
    @Binds
    @Singleton
    abstract fun bindLocationService(
        fusedLocationService: FusedLocationService
    ): LocationService

    /**
     * Binds DefaultRouteOptimizationService to RouteOptimizationService interface.
     */
    @Binds
    @Singleton
    abstract fun bindRouteOptimizationService(
        defaultRouteOptimizationService: DefaultRouteOptimizationService
    ): RouteOptimizationService

    // ============ USER DATA SERVICES ============

    /**
     * Binds LocalFavoritesService to FavoritesService interface.
     */
    @Binds
    @Singleton
    abstract fun bindFavoritesService(
        localFavoritesService: LocalFavoritesService
    ): FavoritesService

    /**
     * Binds LocalPointsService to PointsService interface.
     * Handles gamification points and levels.
     */
    @Binds
    @Singleton
    abstract fun bindPointsService(
        localPointsService: LocalPointsService
    ): PointsService

    /**
     * Binds LocalHistoryService to HistoryService interface.
     * Tracks completed routes.
     */
    @Binds
    @Singleton
    abstract fun bindHistoryService(
        localHistoryService: LocalHistoryService
    ): HistoryService

    /**
     * Binds LocalUserRoutesService to UserRoutesService interface.
     * Handles user-created routes (UGC).
     */
    @Binds
    @Singleton
    abstract fun bindUserRoutesService(
        localUserRoutesService: LocalUserRoutesService
    ): UserRoutesService
}
