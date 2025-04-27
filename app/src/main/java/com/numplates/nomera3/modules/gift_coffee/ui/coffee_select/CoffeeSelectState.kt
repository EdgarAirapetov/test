package com.numplates.nomera3.modules.gift_coffee.ui.coffee_select

import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity

sealed class CoffeeSelectState {
    object Loading : CoffeeSelectState()
    object Init : CoffeeSelectState()
    data class Selected(val type: CoffeeType) : CoffeeSelectState()
    data class Success(val coffeeType: CoffeeType, val promocode: PromoCodeEntity) : CoffeeSelectState()
    data class Error(val exception: Exception) : CoffeeSelectState()
}
