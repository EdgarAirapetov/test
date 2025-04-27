package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListMutualUsersDto(
    @SerializedName("mutual")
    val mutualUserList: List<UserSimple?>?
) : Parcelable
