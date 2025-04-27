package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Countries(
        @SerializedName("countries") var countries: List<Country>?
): Serializable
