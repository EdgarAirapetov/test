package com.numplates.nomera3.modules.post_view_statistic.data

import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector

interface PostViewStatisticRepository {
    fun detectPostView(postViewModel: PostCollisionDetector.PostViewDetectModel)
    fun tryUploadPostViews()
}