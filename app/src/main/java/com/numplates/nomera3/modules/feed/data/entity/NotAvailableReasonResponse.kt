package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

enum class NotAvailableReasonResponse {
    @SerializedName("post not found")
    POST_NOT_FOUND,
    @SerializedName("user not creator")
    USER_NOT_CREATOR,
    @SerializedName("post deleted")
    POST_DELETED,
    @SerializedName("post not published")
    EVENT_POST_UNABLE_TO_UPDATE,
    @SerializedName("update time is over")
    UPDATE_TIME_IS_OVER
}
