package com.numplates.nomera3.modules.peoples.ui.content.preloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity

interface BloggerItemsImagesProvider{
    fun provide(position: Int): List<BloggerMediaContentUiEntity>
}

class BloggerPreloadModelProvider(
    private val provider: BloggerItemsImagesProvider,
    private val context: Context
) : ListPreloader.PreloadModelProvider<BloggerMediaContentUiEntity> {

    override fun getPreloadItems(position: Int): List<BloggerMediaContentUiEntity> = provider.provide(position)

    override fun getPreloadRequestBuilder(item: BloggerMediaContentUiEntity): RequestBuilder<*>? {
        val image = getImageForBlogger(item)?: return null
        return Glide.with(context)
            .load(image)
            .transition(DrawableTransitionOptions.withCrossFade())
    }

    private fun getImageForBlogger(item: BloggerMediaContentUiEntity) = when (item) {
        is BloggerMediaContentUiEntity.BloggerImageContentUiEntity -> item.imageUrl
        is BloggerMediaContentUiEntity.BloggerVideoContentUiEntity -> item.preview
        is BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity -> null
    }
}
