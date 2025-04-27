package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class GiftListEvents {
    object OnMarketError: GiftListEvents()
    object OnRequestGiftsError: GiftListEvents()
}