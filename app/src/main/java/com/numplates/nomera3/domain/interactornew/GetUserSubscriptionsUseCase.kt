package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserSubscriptionsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun invoke(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ) = userRepository.getUserSubscription(
        userId = userId,
        limit = limit,
        offset = offset,
        querySearch = querySearch
    )
}