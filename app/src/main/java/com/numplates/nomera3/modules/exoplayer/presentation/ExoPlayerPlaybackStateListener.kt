package com.numplates.nomera3.modules.exoplayer.presentation

interface ExoPlayerPlaybackStateListener {
    fun onLoading()
    fun onPlaying()
    fun onPause()
    fun onError()
}
