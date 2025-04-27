package com.numplates.nomera3.modules.notifications.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto

data class UserEntityResponse(
        @SerializedName("user_id")
        val userId: Int,

        @SerializedName("account_type")
        val accountType: Int,

        @SerializedName("name")
        val name: String,

        @SerializedName("avatar")
        val avatar: AvatarMetaEntityResponse?,

        @SerializedName("gender")
        val gender: Int,

        @SerializedName("account_color")
        val accountColor: Int,

        @SerializedName("birthday")
        val birthday: Long,

        @SerializedName("moments")
        val moments: UserMomentsDto? = null
)
