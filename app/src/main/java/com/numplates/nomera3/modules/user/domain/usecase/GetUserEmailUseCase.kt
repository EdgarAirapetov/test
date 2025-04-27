package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserEmailUseCase @Inject constructor(private val repository: UserRepository) :
    BaseUseCaseCoroutine<DefParams, UserEmail> {
    override suspend fun execute(
        params: DefParams,
        success: (UserEmail) -> Unit,
        fail: (Throwable) -> Unit
    ) = repository.getUserEmail(success, fail)
}
