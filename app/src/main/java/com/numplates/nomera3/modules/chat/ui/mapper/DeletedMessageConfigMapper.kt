package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class DeletedMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        return MessageConfigWrapperUiModel.Deleted(
            isMy = message.isMy,
            statusConfig = getMessageStatusConfig(message)
        )
    }
}
