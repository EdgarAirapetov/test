package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserPhoneUseCase @Inject constructor(private val repository: UserRepository) :
    BaseUseCaseCoroutine<DefParams, UserPhone> {
    override suspend fun execute(
        params: DefParams,
        success: (UserPhone) -> Unit,
        fail: (Throwable) -> Unit
    ) = repository.getUserPhoneNumber(success, fail)
}
