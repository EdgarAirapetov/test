package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * created by Artem on 07.06.18
 */
class GeoCodingEntity : Serializable {

    @SerializedName("Response") var responce: Responce? = null

    class Responce : Serializable {
        @SerializedName("View") var view: List<Result>? = null
    }

    class Result : Serializable {
        @SerializedName("Result") var location: List<ResultItem>? = null
    }

    class ResultItem : Serializable {
        @SerializedName("Location") var location: Loc? = null
    }

    class Loc: Serializable {
        @SerializedName("Address") var address: Addr? = null
    }

    class Addr : Serializable {
        @SerializedName("Label") var label: String? = null
        @SerializedName("Country") var country: String? = null
        @SerializedName("State") var state: String? = null
        @SerializedName("County") var county: String? = null
        @SerializedName("City") var city: String? = null
        @SerializedName("PostalCode") var postalCode: String? = null
    }
}
