package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.view.view.ProfileListItem

import java.io.Serializable

/**
 * created by Artem on 07.06.18
 */
class CarsModels : Serializable {

    @SerializedName("models") var models: List<Model>? = null

    class Model : Serializable, ProfileListItem {
        override val caption: String?
            get() = name
        override val imageUrl: String?
            get() = modelId.toString()
        override val num: String?
            get() = modelId.toString()

        @SerializedName("name") var name: String? = null
//        @SerializedName("make_id") var makeId: String? = null
@SerializedName("model_id")
var modelId: Int? = null
        @SerializedName("model_name")
        var modelName: String? = null
    }
}
