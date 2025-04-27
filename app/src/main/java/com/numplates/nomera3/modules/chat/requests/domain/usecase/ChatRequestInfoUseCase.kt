package com.numplates.nomera3.modules.chat.requests.domain.usecase

import androidx.lifecycle.LiveData
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import com.numplates.nomera3.presentation.view.adapter.newchat.MessagesSettings
import javax.inject.Inject


class ChatRequestInfoUseCase @Inject constructor(
    private val repository: ChatRequestRepositoryImpl
) {

    fun get(): LiveData<List<MessagesSettings?>> = repository.getChatRequestInfo()

    fun invoke() = repository.getChatRequestData()

}
