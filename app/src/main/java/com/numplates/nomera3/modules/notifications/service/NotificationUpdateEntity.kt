package com.numplates.nomera3.modules.notifications.service

import com.google.gson.annotations.SerializedName

data class NotificationActionEntity(
        @SerializedName("id")
        val id: String,

        @SerializedName("ts")
        val ts: Long,

        @SerializedName("action")
        val action: String
)

enum class NotificationActionType(val action: String) {
    READ("read"),
    ADD("add"),
    DELETE("delete"),
    REFRESH("refresh");

    companion object {
        fun from(action: String): NotificationActionType =
                values().firstOrNull { it.action == action } ?: READ
    }
}
