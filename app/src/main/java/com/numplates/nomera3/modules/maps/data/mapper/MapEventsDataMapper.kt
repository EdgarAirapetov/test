package com.numplates.nomera3.modules.maps.data.mapper

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.maps.domain.events.model.AddressModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.ParticipationModel
import com.numplates.nomera3.modules.user.data.mapper.UserSimpleDomainMapper
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto
import com.numplates.nomera3.modules.userprofile.data.mapper.UserProfileDtoToDbMapper
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.presentation.utils.parseUniquename
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class MapEventsDataMapper @Inject constructor(
    private val userProfileDtoToDbMapper: UserProfileDtoToDbMapper,
    private val userSimpleDomainMapper: UserSimpleDomainMapper
) {

    fun mapEventModel(eventDto: EventDto): EventModel {
        val zoneId = ZoneId.of(eventDto.address.timeZone)
        val eventZonedDateTime = Instant.parse(eventDto.startTime).atZone(zoneId)
        val address = AddressModel(
            name = eventDto.address.name,
            addressString = eventDto.address.addressString,
            location = CoordinatesModel(lat = eventDto.address.location.lat, lon = eventDto.address.location.lon),
            timeZoneId = eventDto.address.timeZone
        )
        val participation = ParticipationModel(
            participantsCount = eventDto.participation.participantsCount,
            isHost = eventDto.participation.isHost.toBoolean(),
            isParticipant = eventDto.participation.isParticipant.toBoolean(),
            newParticipants = eventDto.participation.newlyApplied
        )
        return EventModel(
            id = eventDto.id,
            title = eventDto.title,
            tagSpan = parseUniquename(eventDto.title, eventDto.tags),
            address = address,
            timestampMs = eventZonedDateTime.toInstant().toEpochMilli(),
            timeZoneId = eventDto.address.timeZone,
            eventType = EventType.fromValue(eventDto.eventType),
            participantAvatars = eventDto.participants.map { it.avatarSmall },
            participation = participation
        )
    }

    fun mapParticipants(userDtoList: List<UserSimpleDto>): List<UserSimpleModel> = userDtoList.map {
        userSimpleDomainMapper.mapToDomain(userProfileDtoToDbMapper.mapToUserSimple(it))
    }

    fun mapTypeString(types: List<EventType>): String =
        types.ifEmpty { EventType.values().asList() }
            .map { it.value }
            .joinToString(",")
}
