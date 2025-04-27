package com.numplates.nomera3.modules.userprofile.domain.maper

import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.userprofile.domain.model.AvatarModel
import com.numplates.nomera3.modules.userprofile.domain.model.GalleryItemModel
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import javax.inject.Inject

class PhotoModelMapper @Inject constructor(
    private val featureTogglesContainer: FeatureTogglesContainer
) {
    fun userGalleryModelToPhotoModel(userGalleryItemModel: GalleryItemModel): PhotoModel {
        return PhotoModel(
            id = userGalleryItemModel.id,
            imageUrl = userGalleryItemModel.link,
            post = userGalleryItemModel.post,
            isAdult = userGalleryItemModel.isAdult && featureTogglesContainer.is18plusProfileFeatureToggle.isEnabled
        )
    }

    fun avatarModelToPhotoModel(avatarModel: AvatarModel): PhotoModel {
        return PhotoModel(
            id = avatarModel.id,
            imageUrl = avatarModel.big,
            animation = avatarModel.animation,
            post = avatarModel.post,
            isAdult = avatarModel.isAdult && featureTogglesContainer.is18plusProfileFeatureToggle.isEnabled
        )
    }
}
