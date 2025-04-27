package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.view.view.ProfileListItem

import java.io.Serializable


data class Country(

        @SerializedName("name") var name: String? = null,
        @SerializedName("flag") var flag: String? = null,
        @SerializedName("country_id") var countryId: Int? = null

) : Serializable, ProfileListItem {
    override val caption: String?
        get() = name!!
    override val imageUrl: String?
        get() = flag!!
    override val num: String?
        get() = countryId.toString()

    companion object {
        const val ID_RUSSIA = 3159
        const val ID_UKRAINE = 9908
        const val ID_GEORGIA = 1280
        const val ID_BELARUS = 248
        const val ID_ARMENIA = 245
        const val ID_KAZAKHSTAN = 1894
    }
}
