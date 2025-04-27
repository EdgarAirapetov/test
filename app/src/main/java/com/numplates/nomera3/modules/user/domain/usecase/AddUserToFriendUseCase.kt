package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepositoryImpl
import javax.inject.Inject

class AddUserToFriendUseCase @Inject constructor(
    private val repository: UserRepositoryImpl
) : BaseUseCaseCoroutine<AddUserToFriendParams, Boolean> {

    override suspend fun execute(
        params: AddUserToFriendParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.addUserToFriend(params.userId, success, fail)
    }

}

class AddUserToFriendParams(
    val userId: Long
) : DefParams()
