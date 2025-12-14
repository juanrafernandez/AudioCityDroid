package com.jrlabs.audiocity.di

import com.jrlabs.audiocity.data.repository.FirebaseRouteRepository
import com.jrlabs.audiocity.data.repository.LocalTripRepository
import com.jrlabs.audiocity.domain.repository.RouteRepository
import com.jrlabs.audiocity.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding Repository interfaces to implementations.
 *
 * Follows Dependency Inversion Principle:
 * - High-level modules (ViewModels, UseCases) depend on abstractions (Repository interfaces)
 * - Low-level modules (implementations) also depend on those abstractions
 *
 * Benefits:
 * - Easy to swap implementations (e.g., Firebase → Room for offline)
 * - Facilitates testing with mock implementations
 * - Decouples business logic from data source details
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds FirebaseRouteRepository to RouteRepository interface.
     * Can be easily changed to a different implementation (e.g., RoomRouteRepository).
     */
    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        firebaseRouteRepository: FirebaseRouteRepository
    ): RouteRepository

    /**
     * Binds LocalTripRepository to TripRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        localTripRepository: LocalTripRepository
    ): TripRepository
}
