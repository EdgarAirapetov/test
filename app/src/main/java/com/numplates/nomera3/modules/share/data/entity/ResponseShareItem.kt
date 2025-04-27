package com.numplates.nomera3.modules.share.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple
import com.meera.db.models.dialog.DialogEntity

data class ResponseShareItem(
    @SerializedName("id")
    val id: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("room_id")
    val roomId: Long?,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("user")
    val user: UserSimple?,

    @SerializedName("room")
    val room: DialogEntity?
)
