package com.numplates.nomera3.modules.tags.data.entity

import com.google.gson.annotations.SerializedName

data class HashtagTagListModel(
        @SerializedName("tags")
        val tagList: List<HashtagModel>
)

