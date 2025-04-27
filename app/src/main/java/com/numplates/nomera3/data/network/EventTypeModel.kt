package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EventTypeModel(
        @SerializedName("event_type_id")
        var eventTypeId: Int,
        @SerializedName("event_type_text")
        var eventTypeText: String?,
        @SerializedName("event_type_icon")
        var eventTypeIcon: String?,
        @SerializedName("event_type_active_icon")
        var eventTypeActiveIcon: String?
): Serializable