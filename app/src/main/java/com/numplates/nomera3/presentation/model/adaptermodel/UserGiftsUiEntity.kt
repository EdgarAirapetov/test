package com.numplates.nomera3.presentation.model.adaptermodel

import com.meera.db.models.userprofile.GiftEntity

data class UserGiftsUiEntity(
    val giftEntity: GiftEntity,
    val giftViewType: Int,
    val birthdayTextRanges: List<IntRange> = listOf(),
)
