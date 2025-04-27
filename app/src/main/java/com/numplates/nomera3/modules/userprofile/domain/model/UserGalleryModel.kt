package com.numplates.nomera3.modules.userprofile.domain.model

data class UserGalleryModel(
    val items: List<GalleryItemModel>,
    val count: Int,
    val moreItems: Boolean
)
