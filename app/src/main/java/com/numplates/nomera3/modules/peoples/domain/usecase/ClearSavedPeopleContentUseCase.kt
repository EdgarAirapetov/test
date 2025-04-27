package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class ClearSavedPeopleContentUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke() = repository.clearSavedContent()
}
