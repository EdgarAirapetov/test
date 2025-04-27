package com.numplates.nomera3.modules.userprofile.domain.model.usermain

data class GiftModel (
    val id: Long,
    val typeId: Int,
    val isReceived: Boolean,
    val isViewed: Boolean,
    val imageSmall: String
)
