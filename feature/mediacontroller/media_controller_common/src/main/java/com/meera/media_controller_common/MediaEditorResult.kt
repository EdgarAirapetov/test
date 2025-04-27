package com.meera.media_controller_common

import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class MediaEditorResult(
    var uri: Uri?,
    var isVideo: Boolean,
    var media: String?,
    val mediaKeyboard: List<MediaKeyboard>?
) : Parcelable

@Parcelize
data class MediaKeyboard(
    @SerializedName("sticker_id") val stickerId: Int? = null,
    @SerializedName("gif_id") val gifId: String? = null,
    @SerializedName("preview") val preview: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("ratio") val ratio: Double? = null
) : Parcelable, Serializable

