package com.numplates.nomera3.modules.moments.show.presentation.viewstates

interface MomentPlaybackListener {
    fun onResourceReady(isPreview: Boolean = false) {}
    fun onResourceError() {}
    fun onPausePlayback()
    fun onResumePlayback()
    fun onPlaybackEnded()
}
