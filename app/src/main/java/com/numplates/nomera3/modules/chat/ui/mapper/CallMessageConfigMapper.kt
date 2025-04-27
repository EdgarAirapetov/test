package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.uikit.widgets.chat.call.UiKitCallConfig
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class CallMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper,
    private val callDataMapper: CallDataMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val callData = callDataMapper.mapToCallData(message.metadata)
        return MessageConfigWrapperUiModel.Call(
            UiKitCallConfig(
                statusConfig = getMessageStatusConfig(message),
                isMe = callData.isOutgoing,
                iconType = callData.iconType,
                title = callData.title,
                description = callData.description
            )
        )
    }
}
