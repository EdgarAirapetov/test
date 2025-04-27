package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserAccountTypeUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke(): Int = repository.getUserAccountType()
}
