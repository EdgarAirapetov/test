package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserComplainRepository
import javax.inject.Inject

class ReportUserFromChatUseCase @Inject constructor(
    private val repository: UserComplainRepository
) {

    suspend fun invoke(companionId: Long, reasonId: Int, roomId: Long) {
        repository.complainOnUserFromChat(userId = companionId, reasonId = reasonId, roomId = roomId)
    }
}
