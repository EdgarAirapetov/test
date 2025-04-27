package com.meera.db.models.userprofile

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MutualUsersEntity(
    @SerializedName("user_ids")
    @ColumnInfo(name = "user_ids")
    val userIds: List<Int>? = null,

    @SerializedName("users")
    @ColumnInfo(name = "users")
    val userSimple: List<UserSimple>? = null,

    @SerializedName("more_count")
    @ColumnInfo(name = "more_count")
    val moreCount: Int? = 0
) : Parcelable
