package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName

data class SearchGroupEntityResponse(
        @SerializedName("groups")
        val groups: List<GroupEntityResponse>
)