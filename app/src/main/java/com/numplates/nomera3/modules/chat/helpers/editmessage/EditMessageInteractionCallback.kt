package com.numplates.nomera3.modules.chat.helpers.editmessage

interface EditMessageInteractionCallback {
    suspend fun onShowLoadingProgress(messageId: String)
    suspend fun onHideLoadingProgress(messageId: String)
}
