package com.numplates.nomera3.modules.moments.util

import android.content.Context
import com.bumptech.glide.Glide
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.getScreenWidth
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType

private const val VIDEO_PRE_CACHE_SIZE_KB = 400L

/**
 * Утилита прелоадит контент для моментов
 */
class MomentsPreloadUtil(private val applicationContext: Context) {

    private val videoCacheUtil = CacheUtil(applicationContext)

    fun preload(moments: List<MomentPreloadItem>) {
        moments.forEach { moment ->
            when (moment.contentType) {
                MomentContentType.IMAGE.value -> {
                    preloadImage(moment.contentUrl)
                }
                MomentContentType.VIDEO.value -> {
                    preloadVideo(moment.contentUrl)
                }
            }
        }
    }

    private fun preloadVideo(contentUrl: String?) {
        contentUrl?.let { videoCacheUtil.startCache(videoUrl = contentUrl, howMuchToCache = VIDEO_PRE_CACHE_SIZE_KB) }
    }

    private fun preloadImage(contentUrl: String?) {
        Glide.with(applicationContext)
            .load(contentUrl)
            .preload(getScreenWidth(), getScreenHeight())
    }
}

class MomentPreloadItem(
    val momentId: Long,
    val contentUrl: String?,
    val contentType: String?
)
