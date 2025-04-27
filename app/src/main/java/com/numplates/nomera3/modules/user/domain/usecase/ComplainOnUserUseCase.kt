package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserComplainRepository
import javax.inject.Inject

class ComplainOnUserUseCase @Inject constructor(
        private val repository: UserComplainRepository
) : BaseUseCaseCoroutine<ComplainOnUserParams, Boolean> {

    override suspend fun execute(params: ComplainOnUserParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.complainOnUser(params.userId, params.reasonId, success, fail)
    }

    suspend fun invoke(params: ComplainOnUserParams) {
        repository.complainOnUser(
            userId = params.userId,
            reasonId = params.reasonId,
            success =  { Unit },
            fail = { throw FailedToSendComplaintException() }
        )
    }

    class FailedToSendComplaintException: Exception()
}

class ComplainOnUserParams(
    val userId: Long,
    val reasonId: Int
) : DefParams()
