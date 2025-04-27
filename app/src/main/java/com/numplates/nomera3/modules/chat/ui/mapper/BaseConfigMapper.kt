package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import android.text.format.DateFormat
import com.meera.core.extensions.empty
import com.meera.core.utils.getShortTime
import com.meera.uikit.widgets.chat.forward.ForwardMode
import com.meera.uikit.widgets.chat.forward.UiKitForwardConfig
import com.meera.uikit.widgets.chat.header.NameMode
import com.meera.uikit.widgets.chat.header.UiKitHeaderConfig
import com.meera.uikit.widgets.chat.reply.UiKitReplyConfig
import com.meera.uikit.widgets.chat.status.StatusStyle
import com.meera.uikit.widgets.chat.status.UiKitStatusConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel

abstract class BaseConfigMapper(
    private val context: Context,
    private val replyMessageMapper: ReplyMessageMapper
) {

    abstract fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel

    protected fun getMessageForwardConfig(message: MessageUiModel): UiKitForwardConfig? {
        if (message.author == null) return null
        return UiKitForwardConfig(
            label = context.getString(R.string.chat_message_forwarded),
            content = message.author.name ?: String.empty(),
            forwardMode = ForwardMode.DEFAULT
        )
    }

    protected fun getMessageHeaderNameConfig(message: MessageUiModel, isGroupChat: Boolean): UiKitHeaderConfig? {
        return if (isGroupChat && !message.isMy) {
            UiKitHeaderConfig(
                name = message.creator?.name ?: String.empty(),
                mode = NameMode.DEFAULT
            )
        } else {
            null
        }
    }

    protected fun getMessageReplyConfig(message: MessageUiModel): UiKitReplyConfig? {
        return replyMessageMapper.mapMessageToReplyConfig(message)
    }

    protected fun getMessageStatusConfig(
        message: MessageUiModel,
        messageStatusStyle: StatusStyle = StatusStyle.DEFAULT
    ): UiKitStatusConfig {
        return UiKitStatusConfig(
            sendTimeStr = getShortTime(
                millis = message.createdAt,
                is24hourMode = DateFormat.is24HourFormat(context.applicationContext)
            ),
            editedText = context.getString(R.string.chat_edit_message_edit_label),
            edited = message.editedAt > 0,
            sendState = message.sendStatus,
            statusStyle = messageStatusStyle,
            isMe = message.isMy,
        )
    }
}
