package com.numplates.nomera3.modules.moments.show.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentGroupDto(
    @SerializedName("is_mine")
    val isMine: Int = 0,
    @SerializedName("moments")
    val moments: List<MomentItemDto>,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("user")
    val user: UserSimple
) : Parcelable
