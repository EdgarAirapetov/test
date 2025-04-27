package com.numplates.nomera3.modules.maps.ui.events.participants.list.mapper

import com.meera.core.extensions.isTrue
import com.meera.core.utils.getAge
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import javax.inject.Inject

class EventParticipantListUiMapper @Inject constructor() {

    fun mapUiModel(
        participantUsers: List<UserSimpleModel>,
        participantsCount: Int,
        hostUserId: Long?,
        myUserId: Long?,
        isRefreshing: Boolean,
        isLoadingNextPage: Boolean,
        isLastPage: Boolean
    ): EventParticipantsListUiModel {
        return EventParticipantsListUiModel(
            items = mapEventParticipantListItems(
                participantUsers = participantUsers,
                hostUserId = hostUserId,
                myUserId = myUserId
            ),
            participantsCountString = mapParticipantsCountString(participantsCount),
            isRefreshing = isRefreshing,
            isLoadingNextPage = isLoadingNextPage,
            isLastPage = isLastPage,
            participantsCount = participantsCount
        )
    }

    private fun mapEventParticipantListItems(
        participantUsers: List<UserSimpleModel>,
        hostUserId: Long?,
        myUserId: Long?
    ) : List<EventParticipantsListItemUiModel> {
        if (hostUserId == null || myUserId == null) return emptyList()
        return participantUsers.map { user ->
            val age = user.birthday?.let(::getAge)?.let { "$it, " } ?: ""
            val ageLocation = "$age${user.cityName.orEmpty()}"
            EventParticipantsListItemUiModel(
                userId = user.userId,
                name = user.name.orEmpty(),
                uniqueName = user.uniqueName.orEmpty(),
                ageLocation = ageLocation,
                avatarUrl = user.avatarSmall.orEmpty(),
                isHost = user.userId == hostUserId,
                isMe = user.userId == myUserId,
                isFriend = user.settingsFlags?.friendStatus == 2,
                isSubscribed = user.settingsFlags?.subscribedToMe?.isTrue() ?: false
            )
        }
    }

    private fun mapParticipantsCountString(count: Int): String = when (count) {
        in 0..999 -> count.toString()
        else -> "${count / 1000}K+"
    }
}
