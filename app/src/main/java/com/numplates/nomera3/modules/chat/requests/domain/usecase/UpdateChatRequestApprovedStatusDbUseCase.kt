package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import javax.inject.Inject

class UpdateChatRequestApprovedStatusDbUseCase @Inject constructor(
    private val repository: ChatRequestRepositoryImpl
) : BaseUseCaseCoroutine<UpdateChatRequestApprovedStatusDbParams, Boolean> {

    override suspend fun execute(
        params: UpdateChatRequestApprovedStatusDbParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.updateChatRequestApprovedStatusDb(params.roomId, params.approvedStatus, success, fail)
    }

}

class UpdateChatRequestApprovedStatusDbParams(
    val roomId: Long,
    val approvedStatus: Int
) : DefParams()
