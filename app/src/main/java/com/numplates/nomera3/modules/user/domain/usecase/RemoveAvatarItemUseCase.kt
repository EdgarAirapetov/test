package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class RemoveAvatarItemUseCase @Inject constructor(private val repository: UserRepository) {
    suspend fun invoke(avatarItemId: Long) = repository.deleteAvatarItem(avatarItemId = avatarItemId)
}
