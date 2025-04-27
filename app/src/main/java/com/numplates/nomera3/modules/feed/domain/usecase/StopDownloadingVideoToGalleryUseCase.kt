package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class StopDownloadingVideoToGalleryUseCase @Inject constructor(private val repository: PostRepository) {
    fun invoke(postId: Long) = repository.stopDownloadingPostVideoToGallery(postId)
}
