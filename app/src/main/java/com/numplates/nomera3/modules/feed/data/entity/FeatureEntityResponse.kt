package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.UniquenameEntity

private const val DEF_ID = -1L

data class FeatureEntityResponse(
    @SerializedName("id")
    val id: Long = DEF_ID,

    @SerializedName("aspect")
    val aspect: Double = 1.0,

    @SerializedName("text")
    val text: String?,

    @SerializedName("button")
    val button: String?,

    @SerializedName("deep_link")
    val deepLink: String?,

    @SerializedName("hideable")
    val hideable: Boolean,

    @SerializedName("positions")
    val positions: List<Int>,

    @SerializedName("tags")
    val tags: List<UniquenameEntity>?,

    @SerializedName("duration")
    val videoDurationInSeconds: Int? = null,

    @SerializedName("video")
    val video: String? = null,

    @SerializedName("video_preview")
    val videoPreview: String? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("small_image")
    val smallImage: String? = null,

    @SerializedName("is_closable")
    val isClosable: Int,

    @SerializedName("dismiss_button")
    val dismissButton: String,
)


