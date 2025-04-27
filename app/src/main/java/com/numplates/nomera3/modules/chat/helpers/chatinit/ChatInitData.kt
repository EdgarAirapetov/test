package com.numplates.nomera3.modules.chat.helpers.chatinit

import android.os.Parcelable
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChatInitData(
    val initType: ChatInitType,
    val userId: Long? = null,
    val roomId: Long? = null,
    val isCorner: Boolean = false,
    val isDraggable: Boolean = false,
    val fromMap: Boolean = false,
) : Parcelable

enum class ChatInitType {
    FROM_LIST_ROOMS, FROM_PROFILE
}

data class ChatInitProfileData(
    val mainInfo: ChatInitProfileMainData,
    val blockInfo: ChatInitProfileBlockData,
    val settings: ChatInitProfileSettingsData,
    val style: ChatInitRoomStyleData = ChatInitRoomStyleData(),
    val moments: UserMomentsModel?
)

data class ChatInitProfileMainData(
    val userId: Long?,
    val name: String?,
    val avatar: String?,
    val birthDate: Long?,
    val role: String?,
)

data class ChatInitProfileBlockData(
    val blacklistedByMe: Int?,
    val blacklistedMe: Int?,
)

data class ChatInitProfileSettingsData(
    val iCanChat: Int?,
    val userCanChatMe: Int?,
    val iCanCall: Int?,
    val userCanCallMe: Int?,
    val notificationsOff: Int?,
    val subscriptionOn: Int?,
    val subscribedToMe: Int?,
    val friendStatus: Int?,
    val iCanGreet: Int?,
)

data class ChatInitRoomStyleData(
    val styleBackground: String = "",
    val styleType: String = ""
)


