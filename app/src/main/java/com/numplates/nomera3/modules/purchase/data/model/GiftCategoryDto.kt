package com.numplates.nomera3.modules.purchase.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GiftCategoryDto(
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("gifts") val gifts: List<GiftItemDto>
) : Serializable
