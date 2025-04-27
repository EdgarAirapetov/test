package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.model.EventModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetEventParticipantsParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventSnippetsFullParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventsParamsModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import kotlinx.coroutines.flow.Flow

interface MapEventsRepository {
    suspend fun getMapEvents(params: GetMapEventsParamsModel): List<PostEntityResponse>
    suspend fun getMapEventSnippets(params: GetMapEventSnippetsFullParamsModel): List<PostEntityResponse>
    suspend fun getActiveEventCount(): Int
    fun needToShowEventModerationDialog(): Boolean
    fun setEventModerationDialogShown()
    fun setEventsOnboardingShown()
    fun needToShowEventsOnboarding(): Boolean
    suspend fun getEvent(postId: Long): EventModel
    suspend fun getEventParticipants(params: GetEventParticipantsParamsModel): List<UserSimpleModel>
    suspend fun joinEvent(eventId: Long): PostEntityResponse
    suspend fun leaveEvent(eventId: Long): PostEntityResponse
    suspend fun getEventPost(postId: Long, refreshNewParticipants: Boolean): PostEntityResponse
    fun observeEventParticipationChanges(): Flow<PostEntityResponse>
    suspend fun removeEventParticipant(eventId: Long, userId: Long): PostEntityResponse
}
