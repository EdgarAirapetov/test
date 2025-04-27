package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class SubscribeUserUseCaseNew @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke(userId: Long) = repository.subscribeUser(userId)
}