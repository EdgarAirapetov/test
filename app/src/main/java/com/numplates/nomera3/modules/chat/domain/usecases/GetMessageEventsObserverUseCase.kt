package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class GetMessageEventsObserverUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    fun invoke() = repository.eventsFlow
}
