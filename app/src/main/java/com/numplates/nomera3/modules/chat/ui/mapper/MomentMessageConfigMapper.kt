package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.chat.moment.UiKitMomentConfig
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import javax.inject.Inject

class MomentMessageConfigMapper @Inject constructor(
    private val context: Context,
    private val gson: Gson,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val momentMap = message.attachments?.attachments?.first()?.moment ?: emptyMap()
        val moment = gson.fromJson<MomentItemDto?>(momentMap)
        return MessageConfigWrapperUiModel.Moment(
            UiKitMomentConfig(
                isMe = message.isMy,
                statusConfig = getMessageStatusConfig(message),
                momentDescription = context.getString(R.string.chat_repost_moment_title),
                unavailableText = when {
                    moment?.deleted.toBoolean() -> context.getString(R.string.chat_repost_moment_unavailable)
                    else -> null
                },
                contentAuthorName = moment?.user?.name,
                contentAuthorVerified = moment?.user?.profileVerified.toBoolean(),
                contentAuthorUserpic = UserpicUiModel(
                    size = UserpicSizeEnum.Size24,
                    userName = moment?.user?.name,
                    userAvatarUrl = moment?.user?.avatarSmall,
                    storiesState = UserpicStoriesStateEnum.NO_STORIES
                ),
                mediaContent = moment?.asset?.preview,
                messageContent = message.content.rawText,
            )
        )
    }
}
