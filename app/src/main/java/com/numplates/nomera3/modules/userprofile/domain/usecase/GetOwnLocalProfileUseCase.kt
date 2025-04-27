package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import javax.inject.Inject

class GetOwnLocalProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): UserProfileModel? = repository.getOwnLocalProfile()
}
