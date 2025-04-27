package com.meera.db.models.dialog

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DialogStyle(
    @SerializedName("background")
    val background: String,
    @SerializedName("type")
    val type: String,
) : Parcelable
