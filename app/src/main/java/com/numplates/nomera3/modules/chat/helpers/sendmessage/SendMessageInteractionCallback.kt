package com.numplates.nomera3.modules.chat.helpers.sendmessage

interface SendMessageInteractionCallback {

    suspend fun onInsertDbMessage(messageId: String)

    suspend fun onSuccessSendMessage(
        roomId: Long,
        guestId: Long,
        chatType: String
    )

    suspend fun onActionSendMessage(
        messageId: String,
        isSentError: Boolean,
        resultMessage: String
    )

}
