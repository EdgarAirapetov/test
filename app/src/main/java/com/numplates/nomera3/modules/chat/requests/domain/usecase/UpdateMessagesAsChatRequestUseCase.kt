package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import javax.inject.Inject

class UpdateMessagesAsChatRequestUseCase @Inject constructor(
    private val repository: ChatRequestRepositoryImpl
) : BaseUseCaseCoroutine<UpdateMessagesAsChatRequestParams, Boolean> {

    override suspend fun execute(
        params: UpdateMessagesAsChatRequestParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.updateMessagesAsChatRequest(params.roomId, params.isShowBlur, success, fail)
    }

}

class UpdateMessagesAsChatRequestParams(
    val roomId: Long?,
    val isShowBlur: Boolean
) : DefParams()
