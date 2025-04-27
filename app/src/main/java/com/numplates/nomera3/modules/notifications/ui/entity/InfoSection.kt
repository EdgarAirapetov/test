package com.numplates.nomera3.modules.notifications.ui.entity

import com.meera.db.models.notifications.HIGH_PRIORITY


data class InfoSection(
        val id: String,

        val priority: Int = HIGH_PRIORITY,

        val name: String = "",

        val action: String = ""
)
