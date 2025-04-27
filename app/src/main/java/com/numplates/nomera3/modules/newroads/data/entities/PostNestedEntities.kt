package com.numplates.nomera3.modules.newroads.data.entities

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class MediaEntity(
    @SerializedName("album")
    val album: String? = "",

    @SerializedName("album_url")
    val albumUrl: String? = "",

    @SerializedName("artist")
    val artist: String? = "",

    @SerializedName("artist_url")
    val artistUrl: String? = "",

    @SerializedName("recognized")
    val recognized: Boolean? = false,

    @SerializedName("track")
    val track: String? = "",

    @SerializedName("track_id")
    val track_id: String? = "",

    @SerializedName("track_preview_url")
    val trackPreviewUrl: String? = "",

    @SerializedName("track_url")
    val trackUrl: String? = "",

    @SerializedName("type")
    val type: String? = "",
) : Parcelable, Serializable

@Parcelize
data class MediaKeyboardEntity(
    @SerializedName("sticker_id") val stickerId: Int? = null,
    @SerializedName("gif_id") val gifId: String? = null,
    @SerializedName("preview") val preview: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("ratio") val ratio: Double? = null,
) : Parcelable, Serializable

@Parcelize
data class MediaEntityData(
    @SerializedName("media") val media: MediaEntity? = null,
    @SerializedName("media_keyboard") val mediaKeyboard: List<MediaKeyboardEntity>? = null
) : Parcelable, Serializable

fun MediaEntity.toJson(): String = Gson().toJson(this) ?: String.empty()
fun String.jsonToMedia(): MediaEntity = Gson().fromJson(this, MediaEntity::class.java)

fun List<UploadMediaModel>.toJson(): String = Gson().toJson(this)
fun String.jsonToMediaList(): List<UploadMediaModel> = Gson().fromJson(this,
    object : TypeToken<List<UploadMediaModel>>() {}.type
)

@Parcelize
data class EventEntity(
    @SerializedName("title")
    val title: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("name")
    val name: String,

    @SerializedName("time_zone")
    val timezone: String,

    @SerializedName("type")
    val type: Int,

    @SerializedName("place_id")
    val placeId: Long,
) : Parcelable, Serializable

fun EventEntity.toJson(): String = Gson().toJson(this) ?: String.empty()

fun String.jsonToEvent(): EventEntity = Gson().fromJson(this, EventEntity::class.java)
