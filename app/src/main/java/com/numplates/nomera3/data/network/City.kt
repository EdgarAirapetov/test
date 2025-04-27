package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.view.view.ProfileListItem

import java.io.Serializable

/**
 * created by Artem on 07.06.18
 */
data class City (

    @SerializedName("city_id") var cityId: Int = 0,
    @SerializedName("name") var name: String?,
    @SerializedName("country_id") var countryId: Int = 0,
    @SerializedName("title") var title_: String?  = null,
    @SerializedName("country_name") var countryName: String?  = null
): Serializable, ProfileListItem {
    override val caption: String?
        get() = title_
    override val imageUrl: String?
        get() = countryName
    override val num: String?
        get() = cityId.toString()

}
