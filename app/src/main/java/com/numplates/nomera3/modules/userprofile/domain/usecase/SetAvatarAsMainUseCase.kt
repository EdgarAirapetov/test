package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.AvatarModel
import javax.inject.Inject

class SetAvatarAsMainUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend fun invoke(photoId: Long): AvatarModel {
        return repository.setAvatarAsMain(photoId)
    }
}
