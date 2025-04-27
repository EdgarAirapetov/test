package com.numplates.nomera3.modules.chat.ui.helper

import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class MessageFilterHelper @Inject constructor() {

    private val filters = listOf(MessageType.GIFT)

    fun filterMessages(messages: List<MessageUiModel>): List<MessageUiModel> {
        return messages.filter { !filters.contains(it.messageType) }
    }
}
