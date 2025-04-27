package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class GetApprovedUsersUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke(
        limit: Int,
        offset: Int
    ): List<PeopleApprovedUserModel> = repository.getApprovedUsers(
        limit = limit,
        offset = offset
    )
}
