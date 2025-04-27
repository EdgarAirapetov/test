package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class GetInviterIdUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend fun invoke() =
        profileRepository.requestOwnProfileSynch().inviterId

}
