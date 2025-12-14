package com.jrlabs.audiocity.data.mapper

import com.jrlabs.audiocity.data.dto.DestinationDto
import com.jrlabs.audiocity.data.dto.TripDto
import com.jrlabs.audiocity.domain.model.Destination
import com.jrlabs.audiocity.domain.model.Trip
import java.util.Date
import javax.inject.Inject

/**
 * Mapper for converting between Trip DTOs and Domain models.
 */
class TripMapper @Inject constructor() {

    /**
     * Maps a TripDto to a Trip domain model.
     */
    fun toDomain(dto: TripDto): Trip {
        return Trip(
            id = dto.id,
            destinationCity = dto.destinationCity,
            destinationCountry = dto.destinationCountry,
            selectedRouteIds = dto.selectedRouteIds,
            createdAt = Date(dto.createdAt),
            startDate = dto.startDate?.let { Date(it) },
            endDate = dto.endDate?.let { Date(it) },
            isOfflineAvailable = dto.isOfflineAvailable,
            lastSyncDate = dto.lastSyncDate?.let { Date(it) }
        )
    }

    /**
     * Maps a list of TripDtos to Trip domain models.
     */
    fun toDomainList(dtos: List<TripDto>): List<Trip> {
        return dtos.map { toDomain(it) }
    }

    /**
     * Maps a Trip domain model to a TripDto.
     */
    fun toDto(domain: Trip): TripDto {
        return TripDto(
            id = domain.id,
            destinationCity = domain.destinationCity,
            destinationCountry = domain.destinationCountry,
            selectedRouteIds = domain.selectedRouteIds,
            createdAt = domain.createdAt.time,
            startDate = domain.startDate?.time,
            endDate = domain.endDate?.time,
            isOfflineAvailable = domain.isOfflineAvailable,
            lastSyncDate = domain.lastSyncDate?.time
        )
    }

    /**
     * Maps a list of Trip domain models to TripDtos.
     */
    fun toDtoList(domains: List<Trip>): List<TripDto> {
        return domains.map { toDto(it) }
    }

    /**
     * Maps a DestinationDto to a Destination domain model.
     */
    fun toDomain(dto: DestinationDto): Destination {
        return Destination(
            id = dto.id,
            city = dto.city,
            country = dto.country,
            routeCount = dto.routeCount,
            imageUrl = dto.imageUrl,
            isPopular = dto.isPopular
        )
    }

    /**
     * Maps a list of DestinationDtos to Destination domain models.
     */
    fun destinationsToDomainList(dtos: List<DestinationDto>): List<Destination> {
        return dtos.map { toDomain(it) }
    }
}
