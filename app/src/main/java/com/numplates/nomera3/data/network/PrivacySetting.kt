package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Deprecated("Not used in new Privacy settings")
@Parcelize
data class PrivacySetting (
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: @RawValue Any
): Parcelable