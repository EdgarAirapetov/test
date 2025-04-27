package com.numplates.nomera3.modules.chat.toolbar.domain.mapper

import android.content.Context
import com.numplates.nomera3.R

import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.toolbar.data.entity.ChatUserTypingEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatTypingType
import com.meera.core.extensions.empty

class ChatUserTypingMapper(private val context: Context) {

    fun map(entryData: ChatEntryData, typing: ChatUserTypingEntity): String {
        val userName = entryData.roomType.let { type ->
            if (type == ChatRoomType.GROUP) {
                typing.user.name + " "
            } else String.empty()
        }

        val typingHeaderText = when (typing.type) {
            ChatTypingType.TEXT.key ->
                "$userName${context.getString(R.string.group_chat_status_typing_message)}"
            ChatTypingType.AUDIO.key ->
                "$userName${context.getString(R.string.group_chat_status_record_audio)}"
            else -> String.empty()
        }

        return if (typing.roomId == entryData.roomId) {
            typingHeaderText
        } else String.empty()
    }

}