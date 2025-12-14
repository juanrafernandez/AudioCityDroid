package com.jrlabs.audiocity.data.mapper

import com.jrlabs.audiocity.data.dto.StopDto
import com.jrlabs.audiocity.domain.model.Stop
import com.jrlabs.audiocity.domain.model.StopCategory
import javax.inject.Inject

/**
 * Mapper for converting between Stop DTOs and Domain models.
 */
class StopMapper @Inject constructor() {

    /**
     * Maps a StopDto to a Stop domain model.
     */
    fun toDomain(dto: StopDto): Stop {
        return Stop(
            id = dto.id,
            routeId = dto.routeId,
            order = dto.order,
            name = dto.name,
            description = dto.description,
            category = StopCategory.fromString(dto.category),
            latitude = dto.latitude,
            longitude = dto.longitude,
            triggerRadiusMeters = dto.triggerRadiusMeters,
            audioDurationSeconds = dto.audioDurationSeconds,
            imageUrl = dto.imageUrl,
            scriptEs = dto.scriptEs,
            funFact = dto.funFact
        )
    }

    /**
     * Maps a list of StopDtos to Stop domain models.
     */
    fun toDomainList(dtos: List<StopDto>): List<Stop> {
        return dtos.map { toDomain(it) }
    }

    /**
     * Maps a Stop domain model to a StopDto.
     */
    fun toDto(domain: Stop): StopDto {
        return StopDto(
            id = domain.id,
            routeId = domain.routeId,
            order = domain.order,
            name = domain.name,
            description = domain.description,
            category = domain.category.value,
            latitude = domain.latitude,
            longitude = domain.longitude,
            triggerRadiusMeters = domain.triggerRadiusMeters,
            audioDurationSeconds = domain.audioDurationSeconds,
            imageUrl = domain.imageUrl,
            scriptEs = domain.scriptEs,
            funFact = domain.funFact
        )
    }

    /**
     * Maps a list of Stop domain models to StopDtos.
     */
    fun toDtoList(domains: List<Stop>): List<StopDto> {
        return domains.map { toDto(it) }
    }
}
