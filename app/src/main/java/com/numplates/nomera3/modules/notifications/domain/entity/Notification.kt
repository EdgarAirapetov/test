package com.numplates.nomera3.modules.notifications.domain.entity

import java.util.*

data class Notification(
        val id: String,

        val isRead: Boolean,

        val isGroup: Boolean,

        val groupId: String,

        val date: Date,

        val count: Int,

        val users: List<User>,

        val type: String,

        val meta: MetaNotification,

        val infoSection: InfoSection?,

        val dateGroup: String,

        val changedFlag: Int,

        val commentId: Long?
)
