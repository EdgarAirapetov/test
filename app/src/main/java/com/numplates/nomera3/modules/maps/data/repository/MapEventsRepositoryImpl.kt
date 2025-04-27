package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.data.mapper.MapEventsDataMapper
import com.numplates.nomera3.modules.maps.data.model.JoinEventBodyDto
import com.numplates.nomera3.modules.maps.domain.events.model.EventModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetEventParticipantsParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventSnippetsFullParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventsParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@AppScope
class MapEventsRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain,
    private val appSettings: AppSettings,
    private val dataMapper: MapEventsDataMapper
) : MapEventsRepository {

    private var moderationDialogShown = false
    private val participationChangesFlow = MutableSharedFlow<PostEntityResponse>()

    override suspend fun getMapEvents(params: GetMapEventsParamsModel): List<PostEntityResponse> =
        apiMain.getMapEvents(
            minLat = params.bounds.northEast.lat,
            maxLat = params.bounds.southWest.lat,
            minLon = params.bounds.southWest.lon,
            maxLon = params.bounds.northEast.lon,
            typesString = dataMapper.mapTypeString(params.eventTypes),
            timeFilter = params.timeFilter.value,
            limit = MAX_EVENT_COUNT
        ).data

    override suspend fun getMapEventSnippets(params: GetMapEventSnippetsFullParamsModel): List<PostEntityResponse> =
        apiMain.getMapEventSnippets(
            selectedEventId = params.selectedEventId,
            excludedEventIds = params.excludedEventIds.joinToString(","),
            latitude = params.location.lat,
            longitude = params.location.lon,
            typesString = dataMapper.mapTypeString(params.eventTypes),
            timeFilter = params.timeFilter.value,
            limit = params.limit
        ).data

    override suspend fun getActiveEventCount(): Int = apiMain.getActiveEventCount().data.count

    override fun needToShowEventModerationDialog(): Boolean = moderationDialogShown.not()
        && appSettings.readEventsModerationDialogShownCount() < MAX_TIMES_EVENTS_MODERATION_DIALOG_CAN_BE_SHOWN

    override fun setEventModerationDialogShown() {
        moderationDialogShown = true
        val count = appSettings.readEventsModerationDialogShownCount()
        appSettings.writeEventsModerationDialogShownCount(count + 1)
    }

    override fun setEventsOnboardingShown() {
        appSettings.writeMapEventsOnboardingShown()
    }

    override fun needToShowEventsOnboarding(): Boolean {
        return !appSettings.readMapEventsOnboardingShown()
    }

    override suspend fun getEvent(postId: Long): EventModel =
        dataMapper.mapEventModel(apiMain.getEvent(postId).data.event)

    override suspend fun getEventParticipants(params: GetEventParticipantsParamsModel): List<UserSimpleModel> =
        dataMapper.mapParticipants(
            apiMain.getEventParticipants(
                eventId = params.eventId,
                offset = params.offset,
                limit = params.limit
            ).data
        )

    override suspend fun joinEvent(eventId: Long): PostEntityResponse {
        val response = apiMain.joinEvent(JoinEventBodyDto(eventId)).data
        participationChangesFlow.emit(response)
        return response
    }

    override suspend fun leaveEvent(eventId: Long): PostEntityResponse {
        val response = apiMain.leaveEvent(eventId).data
        participationChangesFlow.emit(response)
        return response
    }

    override suspend fun getEventPost(postId: Long, refreshNewParticipants: Boolean): PostEntityResponse {
        val response = apiMain.getEventPost(postId).data
        val result = if (refreshNewParticipants) {
            val currentEvent = response?.event?.participation?.copy(newlyApplied = 0)
                ?.let { response.event?.copy(participation = it) }
            response?.image.orEmpty().let {
                response.copy(
                    image = it,
                    event = currentEvent
                )
            }
        } else {
            response
        }
        participationChangesFlow.emit(result)
        return result
    }

    override fun observeEventParticipationChanges(): Flow<PostEntityResponse> = participationChangesFlow

    override suspend fun removeEventParticipant(eventId: Long, userId: Long): PostEntityResponse {
        val response = apiMain.removeEventParticipant(eventId = eventId, userId = userId).data
        participationChangesFlow.emit(response)
        return response
    }

    companion object {
        private const val MAX_EVENT_COUNT = 20
        private const val MAX_TIMES_EVENTS_MODERATION_DIALOG_CAN_BE_SHOWN = 3
    }
}
