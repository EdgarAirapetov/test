package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class RemoveGalleryItemUseCase @Inject constructor(private val repository: UserRepository) {
    suspend fun invoke(galleryItemId: Long) = repository.deleteGalleryItem(galleryItemId = galleryItemId)
}
