package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.core.network.websocket.ConnectionStatus
import com.numplates.nomera3.modules.chat.domain.ChatRepository
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class ListenSocketStatusUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    fun invoke(): BehaviorSubject<ConnectionStatus> = repository.listenSocketStatus()

}
