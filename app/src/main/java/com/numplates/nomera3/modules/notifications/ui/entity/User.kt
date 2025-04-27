package com.numplates.nomera3.modules.notifications.ui.entity

data class User(
        val userId: Int,

        val accountType: Int,

        val name: String,

        val avatarBig: String,

        val avatarSmall: String,

        val gender: Int,

        val accountColor: Int,

        val birthday: Long,

        val hasMoments: Boolean? = false,

        val hasNewMoments: Boolean? = false
)
