package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListFriendsResponse (
        @SerializedName("friends") var friends: List<UserSimple>?
): Parcelable
