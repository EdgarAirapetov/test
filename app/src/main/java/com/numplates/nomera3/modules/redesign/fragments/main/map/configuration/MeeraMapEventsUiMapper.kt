package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.ParticipationModel
import com.numplates.nomera3.modules.maps.ui.events.model.AddressSearchState
import com.numplates.nomera3.modules.maps.ui.events.model.AddressUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationMarkerState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetDataUiState
import com.numplates.nomera3.modules.maps.ui.events.model.EventTimeUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapCameraState
import com.numplates.nomera3.modules.maps.ui.snippet.model.DataFetchingStateModel
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventConfigurationUiMode
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.presentation.utils.parseUniquename
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

interface MeeraMapEventsUiMapper {
    suspend fun mapEventConfigurationState(
        eventConfigurationUiMode: MeeraEventConfigurationUiMode,
        markerState: EventConfigurationMarkerState,
        selectedType: EventType,
        eventTime: EventTimeUiModel,
        isMyLocationActive: Boolean,
    ): EventConfigurationState

    fun mapDateItems(minimumDate: LocalDate, selectedDate: LocalDate): List<EventDateItemUiModel>
    fun mapMarkerState(
        eventPlace: PlaceModel?,
        addressSearchState: AddressSearchState,
        mapCameraState: MapCameraState
    ): EventConfigurationMarkerState

    fun mapEventParametersUiModel(
        eventPlace: PlaceModel,
        eventTime: EventTimeUiModel,
        eventType: EventType,
        photo: UIAttachmentPostModel?
    ): EventParametersUiModel

    fun mapEventEntity(eventUiModel: EventParametersUiModel): EventEntity
    fun mapEvents(posts: List<PostEntityResponse>): List<EventObjectUiModel>
    fun mapEvent(postEntityResponse: PostEntityResponse): EventObjectUiModel?
    fun mapEventEditingSetupUiModel(eventEntity: EventEntity): EventEditingSetupUiModel
    fun mapEventTime(minimumTimeInstant: Instant, time: LocalTime?, date: LocalDate?, place: PlaceModel?): EventTimeUiModel
    fun mapEventSnippetUiModel(
        selectedEvent: EventObjectUiModel?,
        eventPages: Map<Int, List<EventObjectUiModel>>,
        dataFetchingState: DataFetchingStateModel
    ): EventSnippetDataUiState
    fun mapAuxEventSnippetUiModel(eventObject: EventObjectUiModel?): EventSnippetDataUiState
    fun mapEventObjectUiModel(post: PostUIEntity): EventObjectUiModel?
    fun mapTimePickerModel(eventTime: EventTimeUiModel): TimePickerUiModel
    fun mapEventStartInstant(eventEditingSetupUiModel: EventEditingSetupUiModel): Instant

    companion object {
        fun mapEventUiModel(eventDto: EventDto): EventUiModel {
            val zoneId = ZoneId.of(eventDto.address.timeZone)
            val eventZonedDateTime = Instant.parse(eventDto.startTime).atZone(zoneId)
            val address = AddressUiModel(
                name = eventDto.address.name,
                addressString = eventDto.address.addressString,
                location = LatLng(eventDto.address.location.lat, eventDto.address.location.lon),
                timeZoneId = eventDto.address.timeZone,
                distanceMeters = eventDto.address.distanceMeters
            )
            val participation = ParticipationModel(
                participantsCount = eventDto.participation.participantsCount,
                isHost = eventDto.participation.isHost.toBoolean(),
                isParticipant = eventDto.participation.isParticipant.toBoolean(),
                newParticipants = eventDto.participation.newlyApplied
            )
            return EventUiModel(
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
    }
}
