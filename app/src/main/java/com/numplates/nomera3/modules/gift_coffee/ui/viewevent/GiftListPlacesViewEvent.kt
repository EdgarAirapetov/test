package com.numplates.nomera3.modules.gift_coffee.ui.viewevent

import androidx.annotation.StringRes

sealed class GiftListPlacesViewEvent {

    class OnErrorGetCoffeeAddress(@StringRes val message: Int): GiftListPlacesViewEvent()
}
