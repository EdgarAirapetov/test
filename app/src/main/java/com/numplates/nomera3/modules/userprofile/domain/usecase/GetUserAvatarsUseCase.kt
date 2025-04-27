package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.UserAvatarsModel
import javax.inject.Inject

class GetUserAvatarsUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend fun invoke(userId: Long, limit: Int, offset: Int): UserAvatarsModel {
        return repository.getAvatars(userId, limit, offset)
    }
}
