package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class ChatEnableUseCase @Inject constructor(
    private val repository: UserRepository
) : BaseUseCaseCoroutine<EnableChatParams, Boolean> {

    override suspend fun execute(
        params: EnableChatParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.enablePrivateMessages(listOf(params.userId), success, fail)
    }

    suspend fun invoke(companionUid: Long) {
        repository.enablePrivateMessages(
            userIds =  listOf(companionUid),
            success = { isSuccess -> if (!isSuccess) throw FailedToEnableChatException(companionUid) },
            fail = { exception -> throw exception }
        )
    }
}

class FailedToEnableChatException(val companionUid: Long): Exception()

class EnableChatParams(
    val userId: Long,
) : DefParams()
