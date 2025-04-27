package com.numplates.nomera3.modules.exoplayer.presentation


/**
 * Controls the behavior when swapping the player between different views.
 */
enum class SwitchPlayerBehavior {

    /**
     * Do nothing when detaching and attaching player
     */
    NONE,

    /**
     * Pause the player when detaching and attaching the instance.
     *
     * Can be used when we don't want to empty the buffer e.g. when we use a playlist with multiple
     * [MediaItems][com.google.android.exoplayer2.MediaItem]/[MediaSources][com.google.android.exoplayer2.source.MediaSource]
     */
    PAUSE,

    /**
     * Stop the player when detaching and attaching the instance.
     *
     * Can be used when we don't need the buffer anymore.
     */
    STOP
}
