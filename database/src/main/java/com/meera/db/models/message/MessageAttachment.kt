package com.meera.db.models.message

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class MessageAttachment(

    @SerializedName("id")
    var id: Long? = null,

    @SerializedName("favourite_id")
    var favoriteId: Int? = null,

    @SerializedName("url")
    var url: String = "",

    @SerializedName("lottie_url")
    var lottieUrl: String? = "",

    @SerializedName("webp_url")
    var webpUrl: String? = "",

    @SerializedName("type")
    var type: String = "",

    @SerializedName("metadata")
    var metadata: @RawValue HashMap<String, Any> = hashMapOf()

) : Parcelable {

    val isGifFromGiphy: Boolean
        get() = url.contains("giphy.com")

    companion object {
        const val EMPTY_URL = "empty_url"
    }

    fun makeMetaMessageWithVideo(): MetaMessageWithVideo? {
        if (metadata.isEmpty()) return null

        val duration = metadata["duration"] as? Double
        val isSilent = metadata["is_silent"] as? Boolean
        val lowQuality = metadata["low_quality"] as? String
        val preview = metadata["preview"] as? String
        val ratio = metadata["ratio"] as? Double

        return MetaMessageWithVideo(
            duration ?: 0.0,
            isSilent ?: false,
            lowQuality ?: "",
            preview ?: "",
            ratio ?: 1.0
        )
    }

    val ratio: Double
        get() = metadata["ratio"] as? Double ?: -1.0

    val audioRecognizedText: String
        get() = metadata["recognized_text"] as? String ?: ""

    val waveForm: List<Int>
        get() = metadata["wave_form"] as? List<Int> ?: listOf()

    val moment: LinkedTreeMap<String, Any>?
        get() = metadata["moment"] as? LinkedTreeMap<String, Any>

}

fun MessageAttachment.isDefault(): Boolean {
    return listOf(url, lottieUrl, webpUrl, type).all { it?.isBlank() == true }
        && favoriteId == null
        && metadata.isEmpty()
}

data class MetaMessageWithVideo(
    val duration: Double,
    val isSilent: Boolean,
    val lowQuality: String,
    val preview: String,
    val ratio: Double
)
