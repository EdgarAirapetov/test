package com.numplates.nomera3.modules.moments.show.presentation.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerManager
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerWrapper


class MomentsExoPlayerWrapper(
    token: String,
    private val player: ExoPlayer,
    private val playerManager: ExoPlayerManager<MomentsExoPlayerWrapper>,
    private val mediaSourceFactory: MediaSourceFactory,
) : ExoPlayerWrapper(token, player) {

    override fun isPlaying() = isActivePlayer() && player.isPlaying

    override fun prepare(contentUrl: String) {
        if (isActivePlayer()) {
            val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(contentUrl))
            player.setMediaSource(mediaSource)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
            player.setAudioAttributes(audioAttributes, true)
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
            player.pause()
            player.seekTo(0)
        }
    }

    override fun clearPlayerWrapper() {
        playerManager.clearExoPlayerWrapper(token)
    }

    override fun isActivePlayer() = playerManager.activeToken == token

}
