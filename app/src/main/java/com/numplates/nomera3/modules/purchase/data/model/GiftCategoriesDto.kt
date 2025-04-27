package com.numplates.nomera3.modules.purchase.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GiftCategoriesDto(
    @SerializedName("categories") val categories: List<GiftCategoryDto>
) : Serializable
