package com.numplates.nomera3.modules.viewvideo.presentation.exoplayer

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerManager
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerWrapper

class ViewVideoExoPlayerWrapper(
    token: String,
    private val player: ExoPlayer,
    private val playerManager: ExoPlayerManager<ViewVideoExoPlayerWrapper>,
) : ExoPlayerWrapper(token, player) {

    override fun prepare(contentUrl: String) {
        if (isActivePlayer()) {
            player.setMediaItem(
                MediaItem.Builder()
                    .setMediaId(contentUrl)
                    .setUri(contentUrl)
                    .build()
            )
            player.prepare()
        }
    }

    override fun start() {
        if (isActivePlayer()) {
            player.seekTo(0)
            player.play()
        }
    }

    override fun resume() {
        if (isActivePlayer()) player.play()
    }

    override fun pause() {
        if (isActivePlayer()) player.pause()
    }

    override fun stop() {
        if (isActivePlayer()) {
            player.stop()
        }
    }

    override fun clearPlayerWrapper() {
        playerManager.clearExoPlayerWrapper(token)
    }

    override fun isActivePlayer() = playerManager.activeToken == token

    fun enableSound() {
        if (isActivePlayer()) player.volume = 1f
    }

    fun disableSound() {
        if (isActivePlayer()) player.volume = 0f
    }

    fun isSoundEnabled(): Boolean = player.volume != 0f

    fun duration(): Long = player.duration

}
