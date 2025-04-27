package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserOnlineStatus(
    @SerializedName("online")
    val online: Boolean? = false,

    @SerializedName("last_active")
    val lastActive: Long? = 0
): Parcelable
