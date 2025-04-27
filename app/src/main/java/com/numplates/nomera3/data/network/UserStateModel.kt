package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserStateModel(
        @SerializedName("state_id") var stateId: Int,
        @SerializedName("state_icon_url") var stateIconUrl: String?,
        @SerializedName("state_text") var stateText: String?
): Serializable