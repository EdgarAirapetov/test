package com.numplates.nomera3.modules.notifications.ui.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toInt
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileBlockData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileMainData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileSettingsData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitRoomStyleData
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import javax.inject.Inject

class CachedUserForChatMapper @Inject constructor() {

    fun mapToCachedUser(user: User): ChatInitProfileData {
        return ChatInitProfileData(
            mainInfo = ChatInitProfileMainData(
                userId = user.userId.toLong(),
                name = user.name,
                avatar = user.avatarSmall,
                birthDate = user.birthday,
                role = String.empty()
            ),
            blockInfo = ChatInitProfileBlockData(
                blacklistedByMe = 0,
                blacklistedMe = 0
            ),
            settings = ChatInitProfileSettingsData(
                iCanChat = true.toInt(),
                userCanChatMe = true.toInt(),
                iCanCall = 1,
                userCanCallMe = 1,
                notificationsOff = 1,
                subscriptionOn = true.toInt(),
                subscribedToMe =  1,
                iCanGreet = 1,
                friendStatus = FRIEND_STATUS_CONFIRMED,
            ),
            style = ChatInitRoomStyleData(),
            moments = UserMomentsModel(
                hasNewMoments = user.hasMoments ?: false,
                hasMoments = user.hasNewMoments ?: false,
                countTotal = 0,
                countNew = 0,
                previews = emptyList()
            )
        )
    }

}
