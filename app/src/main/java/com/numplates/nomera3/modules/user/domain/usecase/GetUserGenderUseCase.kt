package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserGenderUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke(): Gender = repository.getUserGender()
}
