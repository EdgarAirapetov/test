package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileConfig
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import javax.inject.Inject

class CommunityMessageConfigMapper @Inject constructor(
    private val context: Context,
    private val gson: Gson,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val metadata = message.attachments?.attachments?.first()?.metadata ?: emptyMap()
        val group = gson.fromJson<CommunityShareEntity?>(metadata)
        return MessageConfigWrapperUiModel.ShareCommunity(
            UiKitShareProfileConfig(
                userpicUiModel = getUserpickData(group),
                title = group?.name ?: String.empty(),
                description = getDescription(group),
                isLocked = group?.private.toBoolean(),
                statusConfig = getMessageStatusConfig(message),
                isMe = message.isMy,
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
                headerConfig = getMessageHeaderNameConfig(message, isGroupChat)
            )
        )
    }

    private fun getUserpickData(group: CommunityShareEntity?): UserpicUiModel {
        val avatarImage = when {
            group?.deleted.toBoolean() -> R.drawable.illustration_42
            !group?.avatar.isNullOrBlank() -> group?.avatar
            else -> R.drawable.ic_placeholder_profile_community
        }
        return UserpicUiModel(
            size = UserpicSizeEnum.Size88,
            userAvatarUrl = avatarImage as? String,
            userAvatarRes = avatarImage as? Int,
        )
    }

    private fun getDescription(group: CommunityShareEntity?): String {
        val stringRes = when {
            group?.deleted.toBoolean() -> R.string.community_unavailable
            group?.private.toBoolean() -> R.string.group_edit_fragment_close_option_name
            else -> R.string.group_edit_fragment_open_option_name
        }
        return context.getString(stringRes)
    }
}
