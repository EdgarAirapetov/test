package com.numplates.nomera3.modules.notifications.data.entity

import com.google.gson.annotations.SerializedName

data class NotificationEntityResponse(
        @SerializedName("id")
        val id: String,

        @SerializedName("read")
        val read: Boolean,

        @SerializedName("is_group")
        val isGroup: Boolean,

        @SerializedName("date")
        val date: Long,

        @SerializedName("count")
        val count: Int,

        @SerializedName("users")
        val users: List<UserEntityResponse>?,

        @SerializedName("type")
        val type: String,

        @SerializedName("meta")
        val meta: MetaNotificationEntityResponse?,

        @SerializedName("date_group")
        val dateGroup: String? = "",

        val groupId: String = ""
)
