package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.UserGalleryModel
import javax.inject.Inject

class GetUserGalleryUseCase @Inject constructor(private val repository: ProfileRepository) {

   //TODO: https://nomera.atlassian.net/browse/BR-24722
    suspend fun invoke(userId: Long, limit: Int, offset: Int): UserGalleryModel {
        return repository.getGallery(userId, limit, offset)
    }
}
