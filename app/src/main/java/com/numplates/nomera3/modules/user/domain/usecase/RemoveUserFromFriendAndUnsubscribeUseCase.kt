package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class RemoveUserFromFriendAndUnsubscribeUseCase @Inject constructor(
    private val repository: UserRepository,
    private val momentsRepository: MomentsRepository,
) : BaseUseCaseCoroutine<RemoveUserFromFriendAndUnsubscribeParams, Boolean> {

    override suspend fun execute(
        params: RemoveUserFromFriendAndUnsubscribeParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        val successWrapper = { isSuccess: Boolean ->
            if (isSuccess) {
                momentsRepository.updateUserSubscriptions(listOf(params.userId), isAdded = false)
            }
            success.invoke(isSuccess)
        }
        repository.removeUserFromFriendAndCancelSubscription(params.userId, successWrapper, fail)
    }

}

class RemoveUserFromFriendAndUnsubscribeParams(
    val userId: Long
) : DefParams()
