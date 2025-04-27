package com.numplates.nomera3.modules.gifservice.data.entity

import com.google.gson.annotations.SerializedName

data class GiphyResponseWrapper<T>(

        @SerializedName("data")
        val data: List<T?>? = null,

        @SerializedName("pagination")
        val pagination: GiphyItemPaginationResponse? = null
)

data class GiphyFullResponse(

        val data: List<GiphyItemResponse?>? = null,

        @SerializedName("pagination")
        val pagination: GiphyItemPaginationResponse? = null
)
