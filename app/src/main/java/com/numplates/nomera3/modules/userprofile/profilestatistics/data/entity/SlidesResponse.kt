package com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity

import com.google.gson.annotations.SerializedName

const val TYPE_VISITORS = "profile_views"
const val TYPE_VIEWS = "post_views"

class SlidesListResponse(
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("slides")
    val slides: List<SlideResponse>
)

class SlideResponse(
    @SerializedName("type")
    val type: String?,
    @SerializedName("count")
    val count: Long?,
    @SerializedName("growth")
    val growth: Long?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("button")
    val button: ButtonContentResponse?,
    @SerializedName("image_url")
    val imageUrl: String?
)

class ButtonContentResponse(
    @SerializedName("text")
    val text: String?,
    @SerializedName("link")
    val link: String?
)