package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import javax.inject.Inject

class ChatRequestAvailabilityUseCase @Inject constructor(
    private val repository: ChatRequestRepositoryImpl
) : BaseUseCaseCoroutine<ChatRequestAvailabilityParams, DialogEntity?> {

    override suspend fun execute(
        params: ChatRequestAvailabilityParams,
        success: (DialogEntity?) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.chatRequestAvailability(params.roomId, params.approved, success, fail)
    }
}

class ChatRequestAvailabilityParams(
    val roomId: Long,
    val approved: Int,
) : DefParams()
