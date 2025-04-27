package com.numplates.nomera3.modules.peoples.ui.content.preloader

import com.bumptech.glide.ListPreloader
import com.meera.core.extensions.GLIDE_THUMBNAIL_SIZE_MULTIPLIER
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import kotlin.math.max

class RelatedPreloadSizeProvider(
    private val screenWidth: Int
) : ListPreloader.PreloadSizeProvider<RecommendedPeopleUiEntity> {

    override fun getPreloadSize(
        item: RecommendedPeopleUiEntity,
        adapterPosition: Int,
        perItemPosition: Int
    ): IntArray? {
        if (item.userAvatarUrl.isEmpty()) return null
        val newAspect = max(MIN_ASPECT, GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
        return intArrayOf(screenWidth, (screenWidth / newAspect).toInt())
    }
}
