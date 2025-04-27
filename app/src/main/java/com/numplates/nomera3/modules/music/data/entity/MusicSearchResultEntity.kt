package com.numplates.nomera3.modules.music.data.entity

import com.google.gson.annotations.SerializedName

class MusicResponseEntity(
        @SerializedName("data")
        val response: List<MusicSearchResultEntity>
)

class MusicSearchResultEntity(
        @SerializedName("album")
        val album: String?,

        @SerializedName("album_url")
        val albumUrl: String?,

        @SerializedName("artist")
        val artist: String?,

        @SerializedName("artist_url")
        val artistUrl: String?,

        @SerializedName("track")
        val track: String?,

        @SerializedName("track_id")
        val trackId: String?,

        @SerializedName("track_preview_url")
        val trackPreviewUrl: String?,

        @SerializedName("track_url")
        val trackUrl: String?
)
