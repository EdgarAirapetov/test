package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: Long, withoutSideEffects: Boolean = false) = repository.requestProfile(userId,withoutSideEffects)

}
