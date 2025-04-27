package com.numplates.nomera3.modules.gifservice.data.entity

import com.google.gson.annotations.SerializedName

data class GiphyItemResponse(

        @SerializedName("id")
        val id: String,

        @SerializedName("type")
        val type: String,

        @SerializedName("url")
        val url: String,

        @SerializedName("title")
        val title: String,

        @SerializedName("images")
        val images: GiphyImages
)

data class GiphyItemPaginationResponse(
        @SerializedName("total_count")
        val totalCount: Int?,

        @SerializedName("count")
        val count: Int?,

        @SerializedName("offset")
        val offset: Int?
)

/**
 * https://developers.giphy.com/docs/api/schema/#image-object
 */
data class GiphyImages(

        @SerializedName("original")
        val original: GiphyImage,

        @SerializedName("fixed_height")
        val fixedHeight: GiphyImage,

        // 100px - good for mobile keyboard
        @SerializedName("fixed_height_small")
        val fixedHeightSmall: GiphyImage,

        @SerializedName("fixed_width")
        val fixedWidth: GiphyImage,

        // 100px - good for mobile keyboard
        @SerializedName("fixed_width_small")
        val fixedWidthSmall: GiphyImage,

)

data class GiphyImage(

        @SerializedName("width")
        val width: String,

        @SerializedName("height")
        val height: String,

        @SerializedName("url")
        val url: String
)
