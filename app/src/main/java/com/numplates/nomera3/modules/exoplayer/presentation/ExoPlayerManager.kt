package com.numplates.nomera3.modules.exoplayer.presentation

import androidx.annotation.CallSuper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import java.util.UUID

/**
 * ExoPlayerManager helps with managing a single instance of the ExoPlayer.
 *
 * It attaches a [playerInstance][ExoPlayer] to the provided view in [attachPlayerView],
 * and returns an [ExoPlayerWrapper], which manages all interactions with the player itself.
 */
abstract class ExoPlayerManager<T : ExoPlayerWrapper>(
    private val playerInstance: ExoPlayer,
    private val mediaSourceFactory: MediaSourceFactory,
    private val listener: ExoPlayerPlaybackStateListener? = null
) {

    /**
     * Currently active [ExoPlayerWrapper] token.
     *
     * Can be used to determine which wrapper should be active.
     */
    var activeToken: String? = null

    private val playerWrappers: MutableMap<String, T> = HashMap()

    open fun attachPlayerView(playerView: PlayerView, token: String?, interruptPlayback: Boolean): T {
        switchPlayer(player = playerInstance, newPlayerView = playerView, interruptPlayback = interruptPlayback)
        addListenerToPlayer()
        val wrapper = getPlayerWrapper(token)
        activeToken = wrapper.token
        return wrapper
    }

    open fun clearExoPlayerWrapper(token: String?) {
        if (token != null) playerWrappers.remove(token)
    }

    @CallSuper
    open fun releasePlayer() {
        playerInstance.release()
        playerWrappers.clear()
    }

    /**
     * Change the [PlayerView] attached to our [ExoPlayer] instance.
     *
     * @param player [ExoPlayer] instance to which [newPlayerView] will be attached.
     * @param newPlayerView view to be attached to a new player.
     * @param interruptPlayback whether we should interrupt the playback of the video or not.
     * Preferably should be handled before switching the actual player, to avoid displaying incorrect data in [newPlayerView]
     */
    protected abstract fun switchPlayer(player: ExoPlayer, newPlayerView: PlayerView, interruptPlayback: Boolean)

    /**
     * Create the [ExoPlayerWrapper] that will be used for interaction with the [ExoPlayer]
     *
     * @param token Used to differentiate between different wrappers of the same player instance,
     * and determine the currently active [ExoPlayerWrapper]
     * @param playerInstance        player to wrap around
     * @param mediaSourceFactory    factory for creating new media sources within the same wrapper
     */
    protected abstract fun createExoPlayerWrapper(
        token: String = UUID.randomUUID().toString(),
        playerInstance: ExoPlayer,
        mediaSourceFactory: MediaSourceFactory
    ): T

    private fun getPlayerWrapper(token: String?): T {
        return when {
            token != null && playerWrappers.containsKey(token) -> {
                playerWrappers[token] ?: createExoPlayerWrapper(
                    token = token,
                    playerInstance = playerInstance,
                    mediaSourceFactory = mediaSourceFactory
                ).also { playerWrappers[token] = it }
            }
            token != null && !playerWrappers.containsKey(token) -> {
                createExoPlayerWrapper(
                    token = token,
                    playerInstance = playerInstance,
                    mediaSourceFactory = mediaSourceFactory
                ).also { playerWrappers[token] = it }
            }
            else -> {
                createExoPlayerWrapper(
                    playerInstance = playerInstance,
                    mediaSourceFactory = mediaSourceFactory
                ).also { playerWrappers[it.token] = it }
            }
        }
    }

    private fun addListenerToPlayer() {
        playerInstance.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING, Player.STATE_IDLE -> listener?.onLoading()
                    else -> Unit
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                when {
                    isPlaying -> listener?.onPlaying()
                    else -> listener?.onPause()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                listener?.onError()
            }
        })
    }

}
