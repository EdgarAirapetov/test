package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityGalleryFloor(
    val listPhotoEntity: List<GalleryPhotoEntity>,
    val accountTypeEnum: AccountTypeEnum,
    val photoCount: Int = 0,
    val isMineGallery: Boolean = true,
    var isLoading: Boolean = false,
    override var isSeparable: Boolean = true
) : UserUIEntity, Separable {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.GALLERY_FLOOR
}

data class GalleryPhotoEntity(
    val id: Long,
    val link: String,
    val isAdult: Boolean,
)
