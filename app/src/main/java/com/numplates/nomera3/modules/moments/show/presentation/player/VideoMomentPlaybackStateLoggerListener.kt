package com.numplates.nomera3.modules.moments.show.presentation.player

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import timber.log.Timber

private const val MOMENTS_PREFIX = "MOMENTS VIDEO:"

class VideoMomentPlaybackStateLoggerListener(
    private val exoPlayer: ExoPlayer?
) : Player.Listener {

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        log().d("$MOMENTS_PREFIX isPlaying = $isPlaying")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> log().d("$MOMENTS_PREFIX STATE_BUFFERING")
            Player.STATE_IDLE -> log().d("$MOMENTS_PREFIX STATE_IDLE")
            Player.STATE_READY -> log().d("$MOMENTS_PREFIX STATE_READY, dur=${exoPlayer?.duration}, contDur=${exoPlayer?.contentDuration}")
            Player.STATE_ENDED -> log().d("$MOMENTS_PREFIX STATE_ENDED")
            else -> log().d("$MOMENTS_PREFIX STATE_UNKNOWN=$playbackState")
        }
    }

    private fun log(): Timber.Tree {
        return Timber.tag("MomentTestView")
    }
}
