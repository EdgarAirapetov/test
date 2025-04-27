package com.numplates.nomera3.modules.feedmediaview.ui.content.state

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed class ViewMultimediaScreenState {
    object Loading : ViewMultimediaScreenState()
    data class Unavailable(val post: PostUIEntity?) : ViewMultimediaScreenState()
    data class MultimediaPostInfo(
        val post: PostUIEntity,
        val isVolumeEnabled: Boolean,
        val isVideoNeedToPlay: Boolean,
        val isCommentsShow: Boolean
    ) : ViewMultimediaScreenState()
}
