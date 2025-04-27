package com.numplates.nomera3.modules.feed.ui.util.preloader

import com.bumptech.glide.ListPreloader
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import kotlin.math.max

class FeedPreloadSizeProvider(private val screenWidth: Int): ListPreloader.PreloadSizeProvider<PostUIEntity> {

    override fun getPreloadSize(item: PostUIEntity, adapterPosition: Int, perItemPosition: Int): IntArray? {
        if (!item.feedType.isValidViewTypeForLoadingImage()) return null
        val newAspect = max(MIN_ASPECT, item.getSingleAspect())
        return intArrayOf(screenWidth, (screenWidth / newAspect).toInt())
    }

    private fun  FeedType.isValidViewTypeForLoadingImage() =
        when(this){
            FeedType.IMAGE_POST,
            FeedType.IMAGE_POST_VIP,
            FeedType.VIDEO_POST,
            FeedType.VIDEO_POST_VIP,
            FeedType.REPOST,
            FeedType.REPOST_VIP,
            FeedType.VIDEO_REPOST,
            FeedType.ANNOUNCEMENT,
            FeedType.VIDEO_REPOST_VIP,
            FeedType.MULTIMEDIA_POST-> true
            else -> false
        }

}
