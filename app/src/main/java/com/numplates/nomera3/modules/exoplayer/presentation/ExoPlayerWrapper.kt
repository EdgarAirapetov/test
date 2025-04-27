package com.numplates.nomera3.modules.exoplayer.presentation

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player


/**
 * Class that wraps around the [ExoPlayer] instance.
 *
 * Multiple wrappers can use the same player instance, so [isActivePlayer] should be used to determine
 * if we can access the underlying [ExoPlayer].
 *
 * @param token Unique string for differentiating between wrappers and determining the active one.
 * @param player Instance of the [ExoPlayer]. Can be shared between different [ExoPlayerWrapper]s, but should be used with care
 */
abstract class ExoPlayerWrapper(
    val token: String,
    private val player: ExoPlayer
) {

    override fun hashCode() = token.hashCode()

    override fun equals(other: Any?) = token == other

    abstract fun prepare(contentUrl: String)

    abstract fun start()

    abstract fun resume()

    abstract fun pause()

    abstract fun stop()

    abstract fun clearPlayerWrapper()

    open fun isPlaying(): Boolean = isActivePlayer() && player.isPlaying

    open fun seekTo(positionMs: Long) {
        if (isActivePlayer()) player.seekTo(positionMs)
    }

    @Player.State
    open fun playbackState(): Int? {
        return if (isActivePlayer()) player.playbackState else null
    }

    open fun currentProgress(): Float {
        return if (isActivePlayer()) player.currentPosition / player.duration.toFloat() else 0f
    }

    open fun currentPosition(): Long {
        return if (isActivePlayer()) player.currentPosition else 0
    }

    open fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    open fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    protected abstract fun isActivePlayer(): Boolean

}
