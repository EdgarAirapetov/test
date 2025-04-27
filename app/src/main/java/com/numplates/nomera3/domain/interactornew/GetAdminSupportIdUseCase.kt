package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetAdminSupportIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke(): Long = repository.getAdminSupportId()
}
