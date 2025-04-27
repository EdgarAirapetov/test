package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.view.view.ProfileListItem

import java.io.Serializable

/**
 * created by Artem on 07.06.18
 */
class CarsMakes : Serializable {

    @SerializedName("brands")
    var makes: List<Make>? = null

    class Make : Serializable, ProfileListItem {
        override val caption: String?
            get() = name
        override val imageUrl: String?
            get() = makeLogo
        override val num: String?
            get() = makeId.toString()
//        val numName: String?
//            get() = brandName


        @SerializedName("brand_id")
        var makeId: Int? = null
        @SerializedName("brand_name")
        var brandName: String? = null
        @SerializedName("name") var name: String? = null
        @SerializedName("logo")
        var makeLogo: String? = null
    }
}
