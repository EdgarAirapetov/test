package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ResponseError

/**
 * Common recent search response data
 */
data class RecentSearchResult<T>(

    @SerializedName("success")
    val success: List<T>,

    @SerializedName("error")
    val error: ResponseError?
)