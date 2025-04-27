package com.numplates.nomera3.modules.complains.data.model

import com.google.gson.annotations.SerializedName

class UserComplaintParams(
    @SerializedName("comment_id") val commentId: Int? = null,
    @SerializedName("post_id") val postId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("moment_id") val momentId: Long? = null,
    @SerializedName("reason_id") val reasonId: Int? = null,
    @SerializedName("room_id") val roomId: Long? = null,
    @SerializedName("with_file") val withFile: Int? = null,
    @SerializedName("comment") val comment: String? = null,
)
