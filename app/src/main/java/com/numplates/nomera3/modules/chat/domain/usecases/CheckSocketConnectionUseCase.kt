package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatRepository
import javax.inject.Inject

class CheckSocketConnectionUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    fun invoke(): Boolean = repository.isWebSocketConnected()

}
