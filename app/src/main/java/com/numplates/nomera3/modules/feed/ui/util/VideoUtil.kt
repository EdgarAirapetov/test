package com.numplates.nomera3.modules.feed.ui.util

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.numplates.nomera3.modules.feed.ui.adapter.FeedAdapter
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraFeedAdapter
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.presentation.view.ui.FEED_POSITION_FOR_MEDIA
import com.numplates.nomera3.presentation.view.ui.FeedRecyclerView
import com.numplates.nomera3.presentation.view.ui.MeeraFeedRecyclerView

object VideoUtil {

    const val PLAYER_MIN_BUFFER_MS = 2 * 1024
    const val PLAYER_MAX_BUFFER_MS = 15 * 1024
    const val PLAYER_BUFFER_FOR_PLAYBACK_MS = 1024
    const val PLAYER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1024

    fun getFeedHolderPlayer(context: Context): ExoPlayer {
        val trackSelector = DefaultTrackSelector(context)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

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
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setRenderersFactory(rendererFactory)
            .build()
    }

    fun getFeedHolderPlayerMediaSource(context: Context, mediaUrl: String): MediaSource {
        val dataSource = DefaultDataSource.Factory(context)

        val cacheDataSource = CacheDataSource.Factory()
            .setCache(MeeraAct.simpleCache!!)
            .setUpstreamDataSourceFactory(dataSource)

        return ProgressiveMediaSource.Factory(cacheDataSource)
            .createMediaSource(MediaItem.fromUri(mediaUrl))
    }

    fun getMeeraFeedHolderPlayerMediaSource(context: Context, mediaUrl: String): MediaSource {
        val dataSource = DefaultDataSource.Factory(context)

        val cacheDataSource = CacheDataSource.Factory()
            .setCache(MeeraAct.simpleCache!!)
            .setUpstreamDataSourceFactory(dataSource)

        return ProgressiveMediaSource.Factory(cacheDataSource)
            .createMediaSource(MediaItem.fromUri(mediaUrl))
    }

    /**
     * Возвращает позицию видео если такое имеется(сколько уже просмотренно)
     **/
    fun getVideoPosition(
        feedAdapter: FeedAdapter?,
        feedRecycler: FeedRecyclerView?,
        position: Int,
        post: PostUIEntity
    ): Long {
        return when (feedAdapter?.getItemViewType(position)) {
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                post.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        feedRecycler.getPlayingVideoPosition()
                    } else {
                        FEED_POSITION_FOR_MEDIA
                    }
                } ?: let {
                    0L
                }
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                post.parentPost?.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        feedRecycler.getPlayingVideoPosition()
                    } else {
                        FEED_POSITION_FOR_MEDIA
                    }
                } ?: let {
                    0L
                }
            }
            else -> 0L
        }
    }

    fun getVideoInitData(
        feedAdapter: MeeraFeedAdapter?,
        feedRecycler: MeeraFeedRecyclerView?,
        position: Int,
        post: PostUIEntity
    ): ViewVideoInitialData {
        var id: String? = null
        var duration = 0L
        var currentPosition = FEED_POSITION_FOR_MEDIA

        when (feedAdapter?.getItemViewType(position)) {
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                post.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                post.parentPost?.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
            FeedType.MULTIMEDIA_POST.viewType -> {
                id = feedRecycler?.getCurrentMediaId()
                val asset = post.assets?.find { it.id == id }
                asset?.video?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
        }

        return ViewVideoInitialData(id = id, position = currentPosition, duration = duration)
    }

    fun getVideoPosition(
        feedAdapter: MeeraFeedAdapter?,
        feedRecycler: MeeraFeedRecyclerView?,
        position: Int,
        post: PostUIEntity
    ): Long {
        return when (feedAdapter?.getItemViewType(position)) {
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                post.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        feedRecycler.getPlayingVideoPosition()
                    } else {
                        FEED_POSITION_FOR_MEDIA
                    }
                } ?: let {
                    0L
                }
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                post.parentPost?.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        feedRecycler.getPlayingVideoPosition()
                    } else {
                        FEED_POSITION_FOR_MEDIA
                    }
                } ?: let {
                    0L
                }
            }
            FeedType.MULTIMEDIA_POST.viewType -> {
                val id = feedRecycler?.getCurrentMediaId()
                val asset = post.assets?.find { it.id == id }
                asset?.video?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        feedRecycler.getPlayingVideoPosition()
                    } else {
                        FEED_POSITION_FOR_MEDIA
                    }
                } ?: run { FEED_POSITION_FOR_MEDIA }
            }
            else -> 0L
        }
    }

    fun getVideoInitData(
        feedAdapter: FeedAdapter?,
        feedRecycler: FeedRecyclerView?,
        position: Int,
        post: PostUIEntity
    ): ViewVideoInitialData {
        var id: String? = null
        var duration = 0L
        var currentPosition = FEED_POSITION_FOR_MEDIA

        when (feedAdapter?.getItemViewType(position)) {
            FeedType.VIDEO_POST.viewType, FeedType.VIDEO_POST_VIP.viewType -> {
                post.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
            FeedType.VIDEO_REPOST.viewType, FeedType.VIDEO_REPOST_VIP.viewType -> {
                post.parentPost?.getVideoUrl()?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
            FeedType.MULTIMEDIA_POST.viewType -> {
                id = feedRecycler?.getCurrentMediaId()
                val asset = post.assets?.find { it.id == id }
                asset?.video?.let {
                    if (feedRecycler?.isPlayingVideo(it) == true) {
                        currentPosition = feedRecycler.getPlayingVideoPosition()
                        duration = feedRecycler.getPlayingVideoDuration()
                    }
                }
            }
        }

        return ViewVideoInitialData(id = id, position = currentPosition, duration = duration)
    }
}
