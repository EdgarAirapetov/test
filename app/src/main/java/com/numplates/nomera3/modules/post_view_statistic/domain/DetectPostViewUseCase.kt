package com.numplates.nomera3.modules.post_view_statistic.domain

import com.numplates.nomera3.modules.post_view_statistic.data.PostViewStatisticRepository
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import javax.inject.Inject

class DetectPostViewUseCase @Inject constructor(private val repository: PostViewStatisticRepository) {
    fun execute(postViewModel: PostCollisionDetector.PostViewDetectModel) {
        repository.detectPostView(postViewModel)
    }
}