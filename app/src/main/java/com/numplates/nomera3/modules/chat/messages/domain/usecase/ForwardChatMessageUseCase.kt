package com.numplates.nomera3.modules.chat.messages.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.messages.data.entity.ForwardMessageEntityResponse
import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepositoryImpl
import javax.inject.Inject

class ForwardChatMessageUseCase @Inject constructor(
    private val repository: MessagesRepositoryImpl
) : BaseUseCaseCoroutine<ForwardChatMessageParams, ResponseWrapper<ForwardMessageEntityResponse>> {

    override suspend fun execute(
        params: ForwardChatMessageParams,
        success: (ResponseWrapper<ForwardMessageEntityResponse>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.forwardMessage(
            messageId = params.messageId,
            roomId = params.roomId,
            userIds = params.userIds,
            roomIds = params.roomIds,
            comment = params.message,
            success = success,
            fail = fail
        )
    }

}

class ForwardChatMessageParams(
    val messageId: String,
    val roomId: Long,
    val userIds: List<Long>?,
    val roomIds: List<Long>?,
    val message: String,
) : DefParams()
