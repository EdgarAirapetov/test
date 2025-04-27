package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class City(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("name")
    val name: String?
): Parcelable
