package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.UniquenameEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeatureEntity(
    @SerializedName("text")
    val text: String?,
    @SerializedName("button")
    val button: String?,
    @SerializedName("deep_link")
    val deep_link: String?,
    @SerializedName("id")
    val id: Long,
    @SerializedName("hideable")
    val hideable: Boolean,
    @SerializedName("positions")
    val positions: List<Int>,
    @SerializedName("tags")
    val tags: List<UniquenameEntity>,
    @SerializedName("aspect")
    val aspect: Double,
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
    @SerializedName("dismiss_button")
    val dismissButton: String? = null,
): Parcelable {

    var generatedId: Long? = null

}
