package com.numplates.nomera3.modules.chat.ui.helper

import com.meera.core.extensions.isSameDay
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_MESSAGE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_DATE_DIVIDER
import javax.inject.Inject

class DateHeaderHelper @Inject constructor() {

    @Suppress("LocalVariableName")
    fun insertedDateDividers(source: List<MessageEntity>?): List<MessageEntity> {
        val mutatedList = mutableListOf<MessageEntity>()
        val _source = source.orEmpty()
        _source.forEachIndexed { index, currentMessage ->
            mutatedList.add(currentMessage)
            val nextMessage = _source.getOrNull(index + 1)
            val shouldInsertDateDivider =
                nextMessage == null || !isSameDay(currentMessage.createdAt, nextMessage.createdAt)
            if (shouldInsertDateDivider) mutatedList.add(createDateDividerItem(currentMessage.createdAt))
        }
        return mutatedList
    }

    private fun createDateDividerItem(dateTimeMs: Long): MessageEntity = MessageEntity().also {
        it.itemType = ITEM_TYPE_DATE_DIVIDER
        it.type = CHAT_ITEM_TYPE_MESSAGE
        it.createdAt = dateTimeMs
    }
}
