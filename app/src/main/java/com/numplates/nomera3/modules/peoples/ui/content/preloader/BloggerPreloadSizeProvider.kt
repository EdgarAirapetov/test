package com.numplates.nomera3.modules.peoples.ui.content.preloader

import com.bumptech.glide.ListPreloader
import com.meera.core.extensions.GLIDE_THUMBNAIL_SIZE_MULTIPLIER
import com.numplates.nomera3.modules.peoples.ui.content.adapter.BloggerMediaViewType
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import kotlin.math.max

const val MIN_ASPECT = 0.75F

class BloggerPreloadSizeProvider(
    private val screenWidth: Int
) : ListPreloader.PreloadSizeProvider<BloggerMediaContentUiEntity> {

    override fun getPreloadSize(
        item: BloggerMediaContentUiEntity,
        adapterPosition: Int,
        perItemPosition: Int
    ): IntArray? {
        if (!item.isValidViewTypeForLoadingImage()) return null
        val newAspect = max(MIN_ASPECT, GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
        return intArrayOf(screenWidth, (screenWidth / newAspect).toInt())
    }

    private fun BloggerMediaContentUiEntity.isValidViewTypeForLoadingImage(): Boolean =
        when (getItemViewType) {
            BloggerMediaViewType.BLOGGER_IMAGE_MEDIA_CONTENT,
            BloggerMediaViewType.BLOGGER_VIDEO_MEDIA_CONTENT -> true
            else -> false
        }
}
