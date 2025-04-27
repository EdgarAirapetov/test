package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import java.lang.Exception
import javax.inject.Inject

class ChatDisableUseCase @Inject constructor(
    private val repository: UserRepository
) : BaseUseCaseCoroutine<DisableChatParams, Boolean> {

    override suspend fun execute(
        params: DisableChatParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.disablePrivateMessages(listOf(params.userId), success, fail)
    }

    suspend fun invoke(companionUid: Long) {
        repository.disablePrivateMessages(
            userIds = listOf(companionUid),
            success = { isSuccess -> if (!isSuccess) throw FailedToDisableChatException(companionUid) },
            fail = { exception -> throw exception }
        )
    }
}

class FailedToDisableChatException(val companionUid: Long): Exception()

class DisableChatParams(
    val userId: Long
) : DefParams()
