package com.numplates.nomera3.modules.feed.ui.util.preloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

class FeedPreloadModelProvider(
    private val provider: PostItemsImagesProvider,
    private val context: Context
) : PreloadModelProvider<PostUIEntity> {

    override fun getPreloadItems(position: Int): List<PostUIEntity> = provider.provide(position)

    override fun getPreloadRequestBuilder(item: PostUIEntity): RequestBuilder<*>? {
        val image = getImageForFeed(item) ?: return null
        return Glide.with(context)
            .load(image)
            .transition(DrawableTransitionOptions.withCrossFade())
    }

    private fun getImageForFeed(item: PostUIEntity) = when (item.feedType) {
        FeedType.IMAGE_POST,
        FeedType.IMAGE_POST_VIP -> item.getImageUrl()
        FeedType.VIDEO_POST,
        FeedType.VIDEO_POST_VIP -> item.getSingleVideoPreview()
        FeedType.REPOST,
        FeedType.REPOST_VIP -> item.parentPost?.getImageUrl()
        FeedType.VIDEO_REPOST,
        FeedType.VIDEO_REPOST_VIP -> item.parentPost?.getSingleVideoPreview()
        FeedType.RATE_US,
        FeedType.PROGRESS,
        FeedType.CREATE_POST,
        FeedType.EMPTY_PLACEHOLDER,
        FeedType.POSTS_VIEWED_PROFILE,
        FeedType.POSTS_VIEWED_ROAD,
        FeedType.POSTS_VIEWED_PROFILE_VIP,
        FeedType.MOMENTS,
        FeedType.SHIMMER_MOMENTS_PLACEHOLDER,
        FeedType.SHIMMER_PLACEHOLDER,
        FeedType.SYNC_CONTACTS,
        FeedType.REFERRAL,
        FeedType.SUGGESTIONS -> null
        FeedType.ANNOUNCEMENT -> item.featureData?.image
        FeedType.MULTIMEDIA_POST -> item.getImageUrl()
    }
}

interface PostItemsImagesProvider {
    fun provide(position: Int): List<PostUIEntity>
}
