package com.numplates.nomera3.modules.peoples.ui.content.entity.blogger

import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.peoples.ui.content.adapter.BloggerMediaViewType
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity

sealed class BloggerMediaContentUiEntity {

    abstract val getItemViewType: BloggerMediaViewType

    data class BloggerVideoContentUiEntity(
        override val getItemViewType: BloggerMediaViewType = BloggerMediaViewType.BLOGGER_VIDEO_MEDIA_CONTENT,
        val rootUser: PeopleInfoUiEntity,
        val videoDuration: Int,
        val preview: String,
        val videoUrl: String,
        val postId: Long
    ) : BloggerMediaContentUiEntity()

    data class BloggerImageContentUiEntity(
        override val getItemViewType: BloggerMediaViewType = BloggerMediaViewType.BLOGGER_IMAGE_MEDIA_CONTENT,
        val imageUrl: String,
        val rootUser: PeopleInfoUiEntity,
        val postId: Long
    ) : BloggerMediaContentUiEntity()

    data class BloggerContentPlaceholderUiEntity(
        override val getItemViewType: BloggerMediaViewType = BloggerMediaViewType.BLOGGER_PLACEHOLDER,
        val user: PeopleInfoUiEntity,
        val userId: Long,
        val placeholderText: String,
        @DrawableRes val placeholderDrawableRes: Int,
    ) : BloggerMediaContentUiEntity()
}
