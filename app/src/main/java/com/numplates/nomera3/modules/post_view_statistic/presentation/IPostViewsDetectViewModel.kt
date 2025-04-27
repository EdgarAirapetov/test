package com.numplates.nomera3.modules.post_view_statistic.presentation

interface IPostViewsDetectViewModel {
    fun detectPostView(postViewDetectModel: PostCollisionDetector.PostViewDetectModel)
    fun uploadPostViews()
}