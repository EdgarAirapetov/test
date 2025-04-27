package com.numplates.nomera3.modules.chat.requests.domain.usecase

import androidx.paging.DataSource
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepository
import javax.inject.Inject

class GetChatRequestRoomsUseCase @Inject constructor(
    private val repository: ChatRequestRepository
) {

    fun invoke(): DataSource.Factory<Int, DialogEntity> = repository.getChatRequestRooms()
}
