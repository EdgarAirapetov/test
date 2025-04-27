package com.numplates.nomera3.modules.feed.ui.util.preloader

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.meera.core.extensions.getScreenWidth
import com.numplates.nomera3.modules.feed.ui.adapter.FeedAdapter
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraFeedAdapter
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

private const val MAX_PRELOAD = 3

fun Fragment.getPreloader(feedAdapter: FeedAdapter?): RecyclerViewPreloader<PostUIEntity>? {
    return context?.let { nonNullContext ->
        val preloadSizeProvider = FeedPreloadSizeProvider(getScreenWidth())
        val modelProvider = FeedPreloadModelProvider(
            provider = object : PostItemsImagesProvider {
                override fun provide(position: Int): List<PostUIEntity> {
                    val item = feedAdapter?.getItem(position)?: return emptyList()
                    return listOf(item)
                }
            },
            context = nonNullContext
        )
        return@let RecyclerViewPreloader(
            Glide.with(this),
            modelProvider,
            preloadSizeProvider,
            MAX_PRELOAD
        )
    }
}

fun Fragment.getPreloader(feedAdapter: MeeraFeedAdapter?): RecyclerViewPreloader<PostUIEntity>? {
    return context?.let { nonNullContext ->
        val preloadSizeProvider = FeedPreloadSizeProvider(getScreenWidth())
        val modelProvider = FeedPreloadModelProvider(
            provider = object : PostItemsImagesProvider {
                override fun provide(position: Int): List<PostUIEntity> {
                    val item = feedAdapter?.getItem(position)?: return emptyList()
                    return listOf(item)
                }
            },
            context = nonNullContext
        )
        return@let RecyclerViewPreloader(
            Glide.with(this),
            modelProvider,
            preloadSizeProvider,
            MAX_PRELOAD
        )
    }
}
