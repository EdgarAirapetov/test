package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class RemoveRelatedUserUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke(userId: Long) = repository.removeRelatedUserByIdDb(userId)
}
