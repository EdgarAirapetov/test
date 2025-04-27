package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class PushFriendStatusChangedUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun invoke(
        userId: Long,
        isSubscribe: Boolean = false
    ) = userRepository.pushFriendStatusChanged(userId, isSubscribe)
}
