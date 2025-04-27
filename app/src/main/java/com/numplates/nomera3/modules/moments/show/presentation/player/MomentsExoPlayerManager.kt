package com.numplates.nomera3.modules.moments.show.presentation.player

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerManager
import com.numplates.nomera3.modules.moments.util.createPlayer
import com.numplates.nomera3.modules.moments.util.createPlayerMediaSourceFactory
import java.lang.ref.WeakReference

class MomentsExoPlayerManager(
    context: Context
) : ExoPlayerManager<MomentsExoPlayerWrapper>(
    createPlayer(context),
    createPlayerMediaSourceFactory(context, Act.simpleCache)
) {

    private var previousPlayerView: WeakReference<PlayerView?> = WeakReference(null)

    override fun switchPlayer(player: ExoPlayer, newPlayerView: PlayerView, interruptPlayback: Boolean) {
        if (newPlayerView.player != player) {
            if (interruptPlayback) player.stop()
            PlayerView.switchTargetView(player, previousPlayerView.get(), newPlayerView)
            previousPlayerView.clear()
            previousPlayerView = WeakReference(newPlayerView)
        }
    }

    override fun createExoPlayerWrapper(
        token: String,
        playerInstance: ExoPlayer,
        mediaSourceFactory: MediaSourceFactory
    ): MomentsExoPlayerWrapper {
        return MomentsExoPlayerWrapper(
            token = token,
            player = playerInstance,
            playerManager = this,
            mediaSourceFactory = mediaSourceFactory,
        )
    }

    override fun releasePlayer() {
        super.releasePlayer()
        previousPlayerView.clear()
    }

}
