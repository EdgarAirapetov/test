package com.numplates.nomera3.modules.moments.show.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class AddMomentResponse(
    @SerializedName("moment_ids")
    val momentIds: List<Long>,

    @SerializedName("data")
    var data: @RawValue HashMap<String, MomentDataResponse> = hashMapOf()
) : Parcelable

@Parcelize
data class MomentDataResponse(
    @SerializedName("duration")
    val duration : Int
) : Parcelable
