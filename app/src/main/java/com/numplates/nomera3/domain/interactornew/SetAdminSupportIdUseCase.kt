package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class SetAdminSupportIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke(adminSupportId: Long) = repository.saveAdminSupportId(adminSupportId)
}
