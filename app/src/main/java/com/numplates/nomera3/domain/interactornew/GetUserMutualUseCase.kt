package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserMutualUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ) =
        repository.getUserMutual(
            userId = userId,
            limit = limit,
            offset = offset,
            querySearch = querySearch
        )
}
