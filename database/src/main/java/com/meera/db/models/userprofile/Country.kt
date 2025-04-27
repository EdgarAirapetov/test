package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Country(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("name")
    val name: String?
): Parcelable
