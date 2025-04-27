package com.numplates.nomera3.modules.notifications.service

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.notifications.data.entity.NotificationEntityResponse

//TODO переименовать после рефакторинга(удаления сущностей для REST)
data class NotificationSocketResponse(
        @SerializedName("status")
        val status: String = "error",

        @SerializedName("ts")
        val ts: Long? = null,

        @SerializedName("events")
        val events: List<NotificationEntityResponse> = listOf()
)

enum class StatusType(val status: String) {
    OK("ok"),
    ERROR("error");

    companion object {
        fun from(status: String): StatusType =
                values().firstOrNull { it.name == status } ?: ERROR
    }
}
