package com.numplates.nomera3.modules.moments.util

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

private const val PLAYER_MIN_BUFFER_MS = 2 * 1024
private const val PLAYER_MAX_BUFFER_MS = 15 * 1024
private const val PLAYER_BUFFER_FOR_PLAYBACK_MS = 1024
private const val PLAYER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1024

fun createPlayer(context: Context): ExoPlayer {
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

    return ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setLoadControl(loadControl)
        .build()
}

fun createPlayerMediaSourceFactory(context: Context, cache: Cache?): MediaSourceFactory {
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
