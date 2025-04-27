package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePeopleContentUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    fun invoke(): Flow<PeopleModel> = repository.observePeopleContentDatabase()
}
