package com.numplates.nomera3.modules.post_view_statistic.domain

import com.numplates.nomera3.modules.post_view_statistic.data.PostViewStatisticRepository
import javax.inject.Inject

class TryUploadPostViewsUseCase @Inject constructor(private val repository: PostViewStatisticRepository) {
    fun execute() {
        repository.tryUploadPostViews()
    }
}