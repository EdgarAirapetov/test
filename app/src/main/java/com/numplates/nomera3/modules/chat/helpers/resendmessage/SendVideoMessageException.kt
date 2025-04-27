package com.numplates.nomera3.modules.chat.helpers.resendmessage

class SendVideoMessageException(
    message: String,
    val messageId: String
) : Exception(message)