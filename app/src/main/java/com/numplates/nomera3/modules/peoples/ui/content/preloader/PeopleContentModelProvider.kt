package com.numplates.nomera3.modules.peoples.ui.content.preloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity

interface PeopleContentImagesProvider {
    fun provide(position: Int) : List<PeoplesContentUiEntity>
}

class PeopleContentModelProvider(
    private val provider: PeopleContentImagesProvider,
    private val context: Context
) : ListPreloader.PreloadModelProvider<PeoplesContentUiEntity> {
    override fun getPreloadItems(position: Int): List<PeoplesContentUiEntity> = provider.provide(position)

    override fun getPreloadRequestBuilder(item: PeoplesContentUiEntity): RequestBuilder<*>? {
        val imageUrl = getImageForPeople(item)
        return Glide.with(context)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
    }

    private fun getImageForPeople(item: PeoplesContentUiEntity) = when (item.getPeoplesActionType()) {
        PeoplesContentType.PEOPLE_INFO_TYPE -> (item as PeopleInfoUiEntity).imageUrl
        else -> null
    }
}
