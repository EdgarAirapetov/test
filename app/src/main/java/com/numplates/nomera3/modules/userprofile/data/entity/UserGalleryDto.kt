package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class UserGalleryDto(
    @SerializedName("items") val items: List<GalleryItemDto>,
    @SerializedName("count") val count: Int,
    @SerializedName("more_items") val moreItems: Int
)
