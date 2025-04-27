package com.numplates.nomera3.modules.feed.ui.viewmodel

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.volume.domain.model.VolumeState

sealed class FeedViewEventPost {
    class UpdatePostEvent(
        val post: UIPostUpdate,
        val adapterPosition: Int? = null
    ): FeedViewEventPost()

    class UpdatePosts(val posts: List<PostUIEntity>): FeedViewEventPost()

    data object ShowMediaExpand: FeedViewEventPost()

    class UpdateVolumeState(
        val volumeState: VolumeState
    ): FeedViewEventPost()

    class UpdatePostValues(
        val post: UIPostUpdate
    ): FeedViewEventPost()

    class ShowTextError(
        val message: String
    ) : FeedViewEventPost()
}
