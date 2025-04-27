package com.numplates.nomera3.modules.peoples.ui.content.preloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity

interface RelatedPreloadImagesProvider {
    fun provide(position: Int): List<RecommendedPeopleUiEntity>
}

class RelatedPreloadModelProvider(
    private val provider: RelatedPreloadImagesProvider,
    private val context: Context
) : ListPreloader.PreloadModelProvider<RecommendedPeopleUiEntity> {

    override fun getPreloadItems(position: Int): List<RecommendedPeopleUiEntity> = provider.provide(position)

    override fun getPreloadRequestBuilder(item: RecommendedPeopleUiEntity): RequestBuilder<*>? {
        val image = item.userAvatarUrl
        return if (item.userAvatarUrl.isNotEmpty()) {
            Glide.with(context)
                .load(image)
                .transition(DrawableTransitionOptions.withCrossFade())
        } else {
            null
        }
    }

}
