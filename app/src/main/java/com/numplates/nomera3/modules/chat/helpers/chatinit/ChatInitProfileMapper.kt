package com.numplates.nomera3.modules.chat.helpers.chatinit

import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.DialogStyle
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.userprofile.UserSettingsFlags
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import javax.inject.Inject

class ChatInitProfileMapper @Inject constructor() {

    fun mapToEmptyRoom(profileData: ChatInitProfileData?): DialogEntity {
        return DialogEntity().apply {
            val moments = profileData?.moments
            companion = UserChat(
                userId = profileData?.mainInfo?.userId,
                name = profileData?.mainInfo?.name,
                avatarSmall = profileData?.mainInfo?.avatar,
                birthDate = profileData?.mainInfo?.birthDate,
                blacklistedByMe = profileData?.blockInfo?.blacklistedByMe,
                blacklistedMe = profileData?.blockInfo?.blacklistedMe,
                settingsFlags = UserSettingsFlags(
                    iCanChat = profileData?.settings?.iCanChat,
                    userCanChatMe = profileData?.settings?.userCanChatMe,
                    iCanCall = profileData?.settings?.iCanCall,
                    userCanCallMe = profileData?.settings?.userCanCallMe,
                    notificationsOff = profileData?.settings?.notificationsOff,
                    subscription_on = profileData?.settings?.subscriptionOn,
                    subscribedToMe = profileData?.settings?.subscribedToMe,
                    friendStatus = profileData?.settings?.friendStatus,
                    iCanGreet = profileData?.settings?.iCanGreet,
                ),
                role = profileData?.mainInfo?.role,
                moments = UserMomentsMapper.mapUserMomentsDto(moments)
            )
            style = DialogStyle(
                background = profileData?.style?.styleBackground ?: "" ,
                type = profileData?.style?.styleType ?: ""
            )
        }
    }

}
