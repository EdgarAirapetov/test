package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.UniquenameEntity

data class PostCreationResponse(
    @SerializedName("post_id")
    val postId: Long = -1L,
    @SerializedName("event_id")
    val eventId: Long? = null,
    @SerializedName("tags")
    val tags : List<UniquenameEntity?>? = null,
    @SerializedName("text")
    val text: String? = null
)
