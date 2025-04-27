package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PhotoGalleryModel(
        @SerializedName("list")
        var list: MutableList<PhotoModel?>?
) : Serializable