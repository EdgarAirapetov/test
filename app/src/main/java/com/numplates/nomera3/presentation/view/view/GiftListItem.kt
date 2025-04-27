package com.numplates.nomera3.presentation.view.view

interface GiftListItem {

    val giftUserId: Long
    val giftIsMine: Int
    val giftGetId: Int
    val giftSmallImage: String?
    val giftImage: String?
    val giftTypeCode: String?
    val giftAddedAt: Int
    val giftPurchaseDate: Int
}