package com.numplates.nomera3.modules.chat.helpers.resendmessage

sealed class ResendType {

    companion object {
        const val BY_MESSAGE_ID_KEY = 1
        const val BY_ROOM_ID_KEY = 2
    }

    /**
     * Переотправка одного неотправленного сообщения
     */
    class ResendByMessageId(val messageId: String, val roomId: Long) : ResendType()

    /**
     * Переотправка всех неотправленных сообщений в комнате
     */
    class ResendByRoomId(val roomId: Long) : ResendType()
}