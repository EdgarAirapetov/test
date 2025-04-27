package com.numplates.nomera3.modules.share.ui.entity

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem


data class UIShareItem(
    val id: String,
    val userId: Long?,
    val roomId: Long?,
    val type: ShareItemTypeEnum,
    val avatar: Any?,
    val title: String?,
    val subTitle: String?,
    val gender: Int?,
    val idResend: Long,
    val isGroupChat: Boolean,
    val accountTypeEnum: AccountTypeEnum,
    val color: Int,
    val approved: Int,
    val isChecked: Boolean = false,
    val verified: Boolean = false
)

enum class ShareItemTypeEnum {
    ROOM, FRIEND, SUBSCRIPTION
}


fun List<ResponseShareItem>.toUIShareItems(resourceManager: ResourceManager): List<UIShareItem> {
    val newList = mutableListOf<UIShareItem>()
    forEach { item ->
        val type = when (item.type) {
            "room" -> ShareItemTypeEnum.ROOM
            "friend" -> ShareItemTypeEnum.FRIEND
            "subscription" -> ShareItemTypeEnum.SUBSCRIPTION
            else -> null
        }
        val avatar = when (type) {
            ShareItemTypeEnum.ROOM -> {
                if (item.room?.type == ROOM_TYPE_GROUP) {
                    item.room.groupAvatar?: R.drawable.group_chat_avatar_circle
                } else {
                    item.room?.companion?.avatarSmall
                }
            }
            ShareItemTypeEnum.FRIEND,
            ShareItemTypeEnum.SUBSCRIPTION -> item.user?.avatarSmall
            else -> null
        }
        val isGroupChat = item.room?.type == ROOM_TYPE_GROUP

        val title = when (type) {
            ShareItemTypeEnum.ROOM -> {
                if (item.room?.type == ROOM_TYPE_GROUP) {
                    item.room.title
                } else {
                    item.room?.companion?.name
                }
            }
            ShareItemTypeEnum.FRIEND -> item.user?.name
            ShareItemTypeEnum.SUBSCRIPTION -> item.user?.name
            else -> ""
        }

        val subTitle = when (type) {
            ShareItemTypeEnum.ROOM -> {
                if (item.room?.type == ROOM_TYPE_GROUP) {
                    resourceManager.getPlurals(
                         idRes = R.plurals.group_members_plural,
                         quantity = item.room.membersCount?: 0
                    )
                } else {
                    "@${item.room?.companion?.uniqueName}"
                }
            }
            ShareItemTypeEnum.FRIEND -> "@${item.user?.uniqueName}"
            ShareItemTypeEnum.SUBSCRIPTION -> "@${item.user?.uniqueName}"
            else -> ""
        }

        val idResend = when (type) {
            ShareItemTypeEnum.ROOM -> {
                item.room?.roomId
            }
            ShareItemTypeEnum.FRIEND,
            ShareItemTypeEnum.SUBSCRIPTION -> item.user?.userId
            else -> null
        }

        val accountTypeEnum = when (type) {
            ShareItemTypeEnum.ROOM -> {
                if (item.room?.type == ROOM_TYPE_GROUP) {
                    AccountTypeEnum.ACCOUNT_TYPE_REGULAR
                } else {
                    createAccountTypeEnum(item.room?.companion?.accountType)
                }
            }
            ShareItemTypeEnum.FRIEND,
            ShareItemTypeEnum.SUBSCRIPTION -> createAccountTypeEnum(item.user?.accountType)
            else -> AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        }

        val color = when (type) {
            ShareItemTypeEnum.ROOM -> {
                0
            }
            ShareItemTypeEnum.FRIEND,
            ShareItemTypeEnum.SUBSCRIPTION -> item.user?.accountColor ?: 0
            else -> 0
        }
        val approved = when (type) {
            ShareItemTypeEnum.ROOM -> {
                if (item.room?.type == ROOM_TYPE_GROUP) {
                    0
                } else {
                    item.room?.companion?.approved ?: 0
                }
            }
            ShareItemTypeEnum.FRIEND,
            ShareItemTypeEnum.SUBSCRIPTION -> item.user?.approved ?: 0
            else -> 0
        }

        if (type != null && idResend != null) {
            newList.add(
                UIShareItem(
                    id = item.id ?: "",
                    userId = item.userId,
                    roomId = item.roomId,
                    type = type,
                    avatar = avatar,
                    title = title,
                    gender = item.user?.gender ?: 0,
                    idResend = idResend,
                    subTitle = subTitle,
                    accountTypeEnum = accountTypeEnum,
                    color = color,
                    approved = approved,
                    isGroupChat = isGroupChat,
                    verified = item.user?.profileVerified.toBoolean()
                )
            )
        }
    }
    return newList
}
