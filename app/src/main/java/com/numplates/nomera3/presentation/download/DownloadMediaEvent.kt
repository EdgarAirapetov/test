package com.numplates.nomera3.presentation.download

import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState

sealed class DownloadMediaEvent {
    data class PostDownloadState(
        val postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType,
        val state: MediaLoadingState
    ) : DownloadMediaEvent()

    data class PostAlreadyDownloading(val postId: Long) : DownloadMediaEvent()
}
