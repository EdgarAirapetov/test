package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import javax.inject.Inject

class DownloadVideoToGalleryUseCase @Inject constructor(private val repository: PostRepository) {
    fun invoke(postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType) {
        repository.downloadPostVideoToGallery(postMediaDownloadType)
    }
}
