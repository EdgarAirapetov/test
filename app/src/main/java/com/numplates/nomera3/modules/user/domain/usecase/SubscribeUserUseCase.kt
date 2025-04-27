package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class SubscribeUserUseCase @Inject constructor(
    private val repository: UserRepository,
    private val momentsRepository: MomentsRepository,
) : BaseUseCaseCoroutine<SubscribeUserParams, Boolean> {

    override suspend fun execute(
        params: SubscribeUserParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        val successWrapper = { isSuccess: Boolean ->
            if (isSuccess) {
                momentsRepository.updateUserSubscriptions(listOf(params.userId), isAdded = true)
            }
            success.invoke(isSuccess)
        }
        repository.subscribeUser(params.userId, successWrapper, fail)
    }

}

class SubscribeUserParams(
    val userId: Long
) : DefParams()
