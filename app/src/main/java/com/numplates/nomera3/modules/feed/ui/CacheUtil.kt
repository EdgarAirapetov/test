package com.numplates.nomera3.modules.feed.ui

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.meera.core.extensions.doAsync
import com.numplates.nomera3.modules.redesign.MeeraAct
import timber.log.Timber

class CacheUtil(context: Context) {
    private val dataSource =
        DefaultDataSource.Factory(context)

    private val simpleCache = MeeraAct.simpleCache

    private val cacheDataSource =
        CacheDataSource.Factory()
            .setUpstreamDataSourceFactory(dataSource)

    fun startCache(videoUrl: String, howMuchToCache: Long? = null) {
        if (simpleCache == null) return

        if (cacheDataSource.cache == null) {
            cacheDataSource.setCache(simpleCache)
        }

        if (simpleCache.keys.contains(videoUrl)) {
            return
        }

        val videoUri = Uri.parse(videoUrl)
        val dataSpec = if (howMuchToCache == null) {
            DataSpec(videoUri)
        } else {
            DataSpec(videoUri, 0, howMuchToCache * 1024, null)
        }

        val progressListener =
            CacheWriter.ProgressListener { requestLength, bytesCached, newBytesCached ->
                // Timber.d("qwer cache video, bytesCached = $bytesCached for uri = $videoUrl ")
            }

        cacheVideo(
            dataSpec,
            cacheDataSource.createDataSource(),
            progressListener,
        )
    }

    private fun cacheVideo(
        dataSpec: DataSpec,
        dataSource: CacheDataSource,
        progressListener: CacheWriter.ProgressListener?,
    ) = doAsync({
        try {
            CacheWriter(
                dataSource,
                dataSpec,
                null,
                progressListener
            ).cache()
        } catch (exception: Exception) {
            Timber.e(exception.message)
        }
    }, {})
}
