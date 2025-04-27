package com.meera.media_controller_common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Aspect(
    @SerializedName("height")
    var height: Int? = null,
    @SerializedName("width")
    var width: Int? = null
) : Parcelable {

    override fun toString(): String {
        return "Aspect(height=$height, width=$width)"
    }
}
