package com.numplates.nomera3.modules.peoples.ui.content.preloader

import com.bumptech.glide.ListPreloader
import com.meera.core.extensions.GLIDE_THUMBNAIL_SIZE_MULTIPLIER
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import kotlin.math.max

class PeopleContentSizeProvider(
    private val screenWidth: Int
) : ListPreloader.PreloadSizeProvider<PeoplesContentUiEntity> {
    override fun getPreloadSize(
        item: PeoplesContentUiEntity,
        adapterPosition: Int,
        perItemPosition: Int
    ): IntArray? {
        if (!item.isViewTypeValidToLoadImage()) return null
        val newAspect = max(MIN_ASPECT, GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
        return intArrayOf(screenWidth, (screenWidth / newAspect).toInt())
    }

    private fun PeoplesContentUiEntity.isViewTypeValidToLoadImage(): Boolean {
        return when (getPeoplesActionType()) {
            PeoplesContentType.PEOPLE_INFO_TYPE -> true
            else -> false
        }
    }
}
