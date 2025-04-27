package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class GetPeopleAllSavedContentUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke(): PeopleModel = repository.getAllContentDatabase()
}
