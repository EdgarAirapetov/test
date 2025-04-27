package com.numplates.nomera3.modules.viewvideo.presentation.exoplayer

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerManager
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerPlaybackStateListener
import java.lang.ref.WeakReference

private const val PLAYER_MIN_BUFFER_MS = 2 * 1024
private const val PLAYER_MAX_BUFFER_MS = 15 * 1024
private const val PLAYER_BUFFER_FOR_PLAYBACK_MS = 1024
private const val PLAYER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1024

class ViewVideoExoPlayerManager(context: Context, listener: ExoPlayerPlaybackStateListener) : ExoPlayerManager<ViewVideoExoPlayerWrapper>(
    createVideoViewPlayer(context),
    createPlayerMediaSourceFactory(context, Act.simpleCache),
    listener
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
    ): ViewVideoExoPlayerWrapper {
        return ViewVideoExoPlayerWrapper(
            token = token,
            player = playerInstance,
            playerManager = this
        )
    }

    override fun releasePlayer() {
        super.releasePlayer()
        previousPlayerView.clear()
    }

}

private fun createVideoViewPlayer(context: Context): ExoPlayer {
    val trackSelector = DefaultTrackSelector(context)
    trackSelector.setParameters(
        trackSelector
            .buildUponParameters()
            .setMaxVideoSizeSd()
    )
    val loadControl = DefaultLoadControl
        .Builder()
        .setBufferDurationsMs(
            PLAYER_MIN_BUFFER_MS,
            PLAYER_MAX_BUFFER_MS,
            PLAYER_BUFFER_FOR_PLAYBACK_MS,
            PLAYER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        .build()

    val rendererFactory = DefaultRenderersFactory(context.applicationContext)
        .setEnableDecoderFallback(true)

    return ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setLoadControl(loadControl)
        .setRenderersFactory(rendererFactory)
        .setMediaSourceFactory(createPlayerMediaSourceFactory(context, Act.simpleCache))
        .build().also { player ->
            player.repeatMode = Player.REPEAT_MODE_ONE
        }
}

private fun createPlayerMediaSourceFactory(context: Context, cache: SimpleCache?): MediaSourceFactory {
    val defaultDataSource = DefaultDataSource.Factory(context)
    val dataSource = if (cache != null) {
        CacheDataSource
            .Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSource)
    } else {
        defaultDataSource
    }
    return ProgressiveMediaSource.Factory(dataSource)

}
