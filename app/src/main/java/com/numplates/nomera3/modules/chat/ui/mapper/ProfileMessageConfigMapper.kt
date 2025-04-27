package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileConfig
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class ProfileMessageConfigMapper @Inject constructor(
    private val context: Context,
    private val gson: Gson,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val metadata = message.attachments?.attachments?.first()?.metadata ?: emptyMap()
        val user = gson.fromJson<UserSimple?>(metadata)
        return MessageConfigWrapperUiModel.ShareProfile(
            UiKitShareProfileConfig(
                userpicUiModel = getUserpickData(user),
                title = user?.name ?: String.empty(),
                description = getDescription(user),
                information = getInformation(user),
                isVerified = isVerified(user),
                isFlame = isTopContentMaker(user),
                statusConfig = getMessageStatusConfig(message),
                isMe = message.isMy,
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
                headerConfig = getMessageHeaderNameConfig(message, isGroupChat)
            )
        )
    }

    private fun isVerified(user: UserSimple?): Boolean {
        return user?.approved.toBoolean()
    }

    private fun isTopContentMaker(user: UserSimple?): Boolean {
        return if (!isVerified(user)) user?.topContentMaker.toBoolean() else false
    }

    private fun getUserpickData(user: UserSimple?): UserpicUiModel {
        val placeholder = if (user?.profileDeleted.toBoolean()) R.drawable.illustration_42 else null
        val imageLink = if (placeholder == null) user?.avatarSmall else null
        return UserpicUiModel(
            size = UserpicSizeEnum.Size88,
            userAvatarUrl = imageLink,
            userAvatarRes = placeholder
        )
    }

    private fun getDescription(user: UserSimple?): String {
        return if (user?.profileDeleted.toBoolean()) {
            context.getString(R.string.user_profile_unavailable)
        } else {
            "@${user?.uniqueName.orEmpty()}"
        }
    }

    private fun getInformation(user: UserSimple?): String {
        return if (user?.profileDeleted.toBoolean()) {
            String.empty()
        } else {
            "${user?.city?.name.orEmpty()}, ${user?.country?.name.orEmpty()}"
        }
    }
}
