package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.model.IImageData
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class PhotoModel(
        @SerializedName("id") var id: Long,
        @SerializedName("userId") var userId: Long,
        @SerializedName("url") var url: String?,
        @SerializedName("smallUrl") var smallUrl: String?
) : Serializable, IImageData, ProfileListItem {
    override val caption: String?
        get() = null
    override val imageUrl: String?
        get() = url
    override val num: String?
        get() = null
}