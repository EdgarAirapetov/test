package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class DeleteFriendSaveSubscriptionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun invoke(userId: Long) = userRepository.deleteFriendSaveSubscription(userId)
}