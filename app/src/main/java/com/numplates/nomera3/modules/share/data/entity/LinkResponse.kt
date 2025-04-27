package com.numplates.nomera3.modules.share.data.entity

import com.google.gson.annotations.SerializedName

data class LinkResponse(

    @SerializedName("deep_link_url")
    val deeplinkUrl: String
)