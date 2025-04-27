package com.meera.media_controller_common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName;
import kotlinx.android.parcel.Parcelize;

@Parcelize
class CropInfo(
    @SerializedName("custom_crop")
    var customCrop: Boolean? = null,
    @SerializedName("force_crop")
    var forceCrop: Boolean? = null,
    @SerializedName("allow_original_crop")
    val allowOriginalCrop: Boolean? = null,
    @SerializedName("bitrate")
    var bitrate: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("crop_rotations")
    var aspectList: List<Aspect>? = null,
    @SerializedName("max_video_duration")
    var maxVideoDurationSec: Int? = null,
    @SerializedName("image_quality")
    val imageQuality: Int? = null,
    @SerializedName("media_width")
    val mediaWidth: Int? = null,
    @SerializedName("media_height")
    val mediaHeight: Int? = null,
) : Parcelable {

    fun needCompressMedia(
        currentWidth: Int = 0,
        currentHeight: Int = 0,
        currentBitrate: Int = 0
    ): Boolean {
        return currentWidth > (mediaWidth ?: 0)
            || currentHeight > (mediaHeight ?: 0)
            || currentBitrate > (bitrate ?: 0)
    }

    override fun toString(): String {
        return "CropInfo(customCrop=$customCrop, forceCrop=$forceCrop, " +
            "bitrate=$bitrate, name=$name, aspect=$aspectList, maxVideoDuration=$maxVideoDurationSec)"
    }
}
