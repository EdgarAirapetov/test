package com.numplates.nomera3.domain.util

import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType

class UserSubscriptionsUiMapper {

    fun mapFromUserSimpleListToUiModel(
        responseList: List<UserSimple?>?,
        myUserId: Long?
    ): List<FriendsFollowersUiModel> {
        val listResult = mutableListOf<FriendsFollowersUiModel>()
        responseList?.forEach { userSimple ->
            listResult.add(
                FriendsFollowersUiModel(
                    userSimple = userSimple,
                    subscriptionType = getSubscriptionType(
                        model = userSimple,
                        myUserId = myUserId
                    ),
                    isAccountApproved = userSimple?.approved?.toBoolean() ?: false
                )
            )
        }
        return listResult
    }

    private fun getSubscriptionType(
        model: UserSimple?,
        myUserId: Long?
    ): SubscriptionType {
        return when {
            model?.settingsFlags?.friendStatus
                    == REQUEST_NOT_CONFIRMED_BY_ME -> SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST
            model?.settingsFlags?.friendStatus == FRIEND_STATUS_NONE
                    && myUserId != model.userId -> SubscriptionType.TYPE_FRIEND_NONE
            else -> SubscriptionType.DEFAULT
        }
    }
}
