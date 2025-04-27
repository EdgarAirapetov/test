package com.numplates.nomera3.modules.userprofile.data.mapper

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.mapper.toPostUIEntity
import com.numplates.nomera3.modules.userprofile.data.entity.GalleryItemDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserGalleryDto
import com.numplates.nomera3.modules.userprofile.domain.model.GalleryItemModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserGalleryModel
import javax.inject.Inject

class UserGalleryMapper @Inject constructor(
    private val featureTogglesContainer: FeatureTogglesContainer
) {

    fun userGalleryDtoToUserGalleryModel(userGalleryDto: UserGalleryDto): UserGalleryModel {
        val galleryItemsModel = userGalleryDto.items.map { galleryItemDtoToGalleryModel(it) }
        return UserGalleryModel(
            items = galleryItemsModel,
            userGalleryDto.count,
            moreItems = userGalleryDto.moreItems.toBoolean()
        )
    }

    fun galleryItemDtoToGalleryModel(galleryItemDto: GalleryItemDto) = GalleryItemModel(
        createdAt = galleryItemDto.createdAt,
        id = galleryItemDto.id,
        link = galleryItemDto.link,
        post = galleryItemDto.post?.toPostUIEntity(),
        postId = galleryItemDto.postId,
        isAdult = (galleryItemDto.isAdult == 1) && featureTogglesContainer.is18plusProfileFeatureToggle.isEnabled
    )
}
