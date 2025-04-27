package com.numplates.nomera3.modules.moments.show.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetMomentGroupsResponseDto(
    @SerializedName("moment_groups")
    val momentGroups: List<MomentGroupDto>,
    @SerializedName("session_id")
    val session: String?
) : Parcelable
