package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class UserCallUnavailableUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    suspend fun execute(
        userId: Long
    ): Boolean =
        profileRepository.makeCallUnvailables(userId)

}
