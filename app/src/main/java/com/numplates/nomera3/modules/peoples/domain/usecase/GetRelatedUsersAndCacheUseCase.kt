package com.numplates.nomera3.modules.peoples.domain.usecase

import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import javax.inject.Inject

class GetRelatedUsersAndCacheUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    suspend fun invoke(
        limit: Int,
        offset: Int
    ) = repository.getRelatedUsersAndCache(
        limit = limit,
        offset = offset
    )
}
