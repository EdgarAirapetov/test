package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class SetProfileViewedUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend fun invoke(userId: Long): ResponseWrapper<EmptyModel> {
        return repository.setProfileViewed(userId = userId)
    }
}
