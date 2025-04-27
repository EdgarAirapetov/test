package com.numplates.nomera3.modules.feed.ui.util.preloader

import android.content.Context
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import javax.inject.Inject

private const val POST_AMOUNT_PRELOAD = 10
private const val DEBOUNCE_INTERVAL_MS = 200
private const val CACHE_SIZE_KB = 400L

class FeedVideoPreLoader @Inject constructor(context: Context) {

    private val cacheUtil = CacheUtil(context)
    private var debounceLastTime = System.currentTimeMillis()

    fun preLoadVideoPosts(currentVisiblePostPosition: Int, allPosts: List<PostUIEntity>) {
        if (!debounceValid()) return

        for (postIndex in currentVisiblePostPosition until currentVisiblePostPosition + POST_AMOUNT_PRELOAD) {
            val postToCheck = allPosts.getOrNull(postIndex)

            if (postToCheck.containsVideo()) {
                val assets = postToCheck?.assets ?: emptyList()
                for (asset in assets) {
                    asset.video?.let { cacheUtil.startCache(videoUrl = it, howMuchToCache = CACHE_SIZE_KB) }
                }
            }
        }
    }

    private fun debounceValid(): Boolean {
        return if (System.currentTimeMillis() - debounceLastTime > DEBOUNCE_INTERVAL_MS) {
            debounceLastTime = System.currentTimeMillis()
            true
        } else {
            false
        }
    }

    private fun PostUIEntity?.containsVideo(): Boolean {
        val video = this?.assets?.firstOrNull { it.type == MEDIA_VIDEO }
        return video != null
    }
}
