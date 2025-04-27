package com.numplates.nomera3.modules.complains.data.model

import com.google.gson.annotations.SerializedName

class ChatComplaintParams(
    @SerializedName("room_id")
    val roomId: Long? = null,

    @SerializedName("reason_id")
    val reasonId: Int? = null,

    @SerializedName("with_file")
    val withFile: Int? = null,

    @SerializedName("comment")
    val comment: String? = null,
)
