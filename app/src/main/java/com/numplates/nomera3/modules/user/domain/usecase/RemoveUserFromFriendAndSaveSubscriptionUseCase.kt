package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepositoryImpl
import javax.inject.Inject

class RemoveUserFromFriendAndSaveSubscriptionUseCase @Inject constructor(
    private val repository: UserRepositoryImpl
) : BaseUseCaseCoroutine<RemoveUserFromFriendAndSaveSubscriptionParams, Boolean> {

    override suspend fun execute(
        params: RemoveUserFromFriendAndSaveSubscriptionParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.removeUserFromFriendAndSaveSubscription(params.userId, success, fail)
    }

}

class RemoveUserFromFriendAndSaveSubscriptionParams(
    val userId: Long
) : DefParams()
