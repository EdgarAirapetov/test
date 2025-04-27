package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class UnsubscribeUserUseCase @Inject constructor(
    private val repository: UserRepository,
    private val momentsRepository: MomentsRepository,
) : BaseUseCaseCoroutine<UnsubscribeUserParams, Boolean> {

    override suspend fun execute(
        params: UnsubscribeUserParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        val successWrapper = { isSuccess: Boolean ->
            if (isSuccess) {
                momentsRepository.updateUserSubscriptions(listOf(params.userId), isAdded = false)
            }
            success.invoke(isSuccess)
        }
        repository.unsubscribeUser(params.userId, successWrapper, fail)
    }

}

class UnsubscribeUserParams(
    val userId: Long
) : DefParams()
