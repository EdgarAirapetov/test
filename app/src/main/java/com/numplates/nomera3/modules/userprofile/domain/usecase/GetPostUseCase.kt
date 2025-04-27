package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class GetPostUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend fun invoke(postId: Long): PostUIEntity {
        return repository.getPost(postId)
    }
}
