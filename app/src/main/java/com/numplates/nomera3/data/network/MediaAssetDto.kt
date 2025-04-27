package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaAssetDto(

    //common fields
    @SerializedName("id")
    var id: String?,

    @SerializedName("type")
    var type: String?,

    @SerializedName("aspect")
    var aspect: Float?,

    @SerializedName("base64_preview")
    var base64Preview: String?,

    @SerializedName("image")
    var image: String?,

    @SerializedName("small_image")
    var smallImage: String?,

    @SerializedName("small_url")
    var smallUrl: String?,

    @SerializedName("media_positioning")
    var mediaPositioning: MediaPositioningDto?,

    //for video
    @SerializedName("video")
    var video: String?,

    @SerializedName("video_preview")
    var videoPreview: String?,

    @SerializedName("duration")
    var duration: Int?,

    //for gif
    @SerializedName("gif_preview")
    var gifPreview: String?,

    @SerializedName("metadata")
    var metadata: AssetMetadata?

) : Parcelable
