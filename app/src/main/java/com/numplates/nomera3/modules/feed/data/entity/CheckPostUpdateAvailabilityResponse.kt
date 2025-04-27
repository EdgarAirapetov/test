package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class CheckPostUpdateAvailabilityResponse(

    @SerializedName("is_available")
    val isAvailable: Int,

    @SerializedName("not_available_reason")
    val notAvailableReason: NotAvailableReasonResponse?
)
