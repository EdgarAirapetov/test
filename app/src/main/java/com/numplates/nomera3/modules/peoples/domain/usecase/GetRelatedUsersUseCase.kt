package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class GetRelatedUsersUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke(
        limit: Int,
        offset: Int,
        selectedUserId: Long? = null
    ): List<PeopleRelatedUserModel> = repository.getRelatedUsers(
        limit = limit,
        offset = offset,
        selectedUserId = selectedUserId
    )
}
