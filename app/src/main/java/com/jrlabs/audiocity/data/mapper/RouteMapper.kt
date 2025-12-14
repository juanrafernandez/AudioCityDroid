package com.jrlabs.audiocity.data.mapper

import com.jrlabs.audiocity.data.dto.LocationDto
import com.jrlabs.audiocity.data.dto.RouteDto
import com.jrlabs.audiocity.domain.model.Location
import com.jrlabs.audiocity.domain.model.Route
import com.jrlabs.audiocity.domain.model.RouteDifficulty
import javax.inject.Inject

/**
 * Mapper for converting between Route DTOs and Domain models.
 * Follows Single Responsibility Principle - only handles Route mapping.
 */
class RouteMapper @Inject constructor() {

    /**
     * Maps a RouteDto to a Route domain model.
     */
    fun toDomain(dto: RouteDto): Route {
        return Route(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            city = dto.city,
            neighborhood = dto.neighborhood,
            durationMinutes = dto.durationMinutes,
            distanceKm = dto.distanceKm,
            difficulty = RouteDifficulty.fromString(dto.difficulty),
            numStops = dto.numStops,
            language = dto.language,
            isActive = dto.isActive,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            thumbnailUrl = dto.thumbnailUrl,
            startLocation = toDomain(dto.startLocation),
            endLocation = toDomain(dto.endLocation)
        )
    }

    /**
     * Maps a list of RouteDtos to Route domain models.
     */
    fun toDomainList(dtos: List<RouteDto>): List<Route> {
        return dtos.map { toDomain(it) }
    }

    /**
     * Maps a LocationDto to a Location domain model.
     */
    fun toDomain(dto: LocationDto): Location {
        return Location(
            latitude = dto.latitude,
            longitude = dto.longitude,
            name = dto.name
        )
    }

    /**
     * Maps a Route domain model to a RouteDto.
     * Useful for caching or sending to backend.
     */
    fun toDto(domain: Route): RouteDto {
        return RouteDto(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            city = domain.city,
            neighborhood = domain.neighborhood,
            durationMinutes = domain.durationMinutes,
            distanceKm = domain.distanceKm,
            difficulty = domain.difficulty.value,
            numStops = domain.numStops,
            language = domain.language,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            thumbnailUrl = domain.thumbnailUrl,
            startLocation = toDto(domain.startLocation),
            endLocation = toDto(domain.endLocation)
        )
    }

    /**
     * Maps a Location domain model to a LocationDto.
     */
    fun toDto(domain: Location): LocationDto {
        return LocationDto(
            latitude = domain.latitude,
            longitude = domain.longitude,
            name = domain.name
        )
    }
}
