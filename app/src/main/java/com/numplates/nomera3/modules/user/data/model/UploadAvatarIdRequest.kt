package com.numplates.nomera3.modules.user.data.model

import com.google.gson.annotations.SerializedName

data class UploadAvatarIdRequest(
    @SerializedName("upload_id") val uploadId: String,
    @SerializedName("avatar_animation") val avatarAnimation: String?,
    @SerializedName("save_settings") val saveSettings: Int,
    @SerializedName("create_avatar_post") val createAvatarPost: Int
)
