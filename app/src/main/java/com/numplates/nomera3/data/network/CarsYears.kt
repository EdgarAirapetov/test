package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * created by Artem on 07.06.18
 */
class CarsYears : Serializable {

    @SerializedName("Years") var years: Years? = null

    class Years : Serializable {
        @SerializedName("min_year") var minYear: String? = null
        @SerializedName("max_year") var maxYear: String? = null
    }
}
