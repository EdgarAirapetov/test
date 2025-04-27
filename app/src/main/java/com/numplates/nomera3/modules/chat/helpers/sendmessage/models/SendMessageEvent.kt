package com.numplates.nomera3.modules.chat.helpers.sendmessage.models

import com.meera.db.models.message.MessageEntity

sealed interface SendMessageEvent

class MessageSendSuccessfullyEvent (
    val message: MessageEntity
): SendMessageEvent
