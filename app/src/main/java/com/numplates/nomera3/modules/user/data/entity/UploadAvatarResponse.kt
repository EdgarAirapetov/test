package com.numplates.nomera3.modules.user.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadAvatarResponse(
    @SerializedName("big")
    var avatarBig: String?,
    @SerializedName("small")
    var avatarSmall: String?,
    @SerializedName("animation")
    var avatarAnimation: String
): Parcelable