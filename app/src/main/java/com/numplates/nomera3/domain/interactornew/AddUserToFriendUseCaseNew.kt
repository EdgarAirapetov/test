package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class AddUserToFriendUseCaseNew @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke(
        userId: Long,
        isRequestFromShake: Boolean = false
    ) = repository.addUserToFriend(
        userId = userId,
        isRequestFromShake = isRequestFromShake
    )
}
