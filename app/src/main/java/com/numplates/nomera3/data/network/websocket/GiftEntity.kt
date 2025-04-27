package com.numplates.nomera3.data.network.websocket

import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType

fun GiftEntity.updatePromoCode(code: String, type: CoffeeType, isViewed: Boolean = true): GiftEntity =
    copy(
        metadata = metadata?.copy(
            coffeeCode = code,
            coffeeType = type.value,
            isViewed = isViewed,
            coffeeCustomDrawable = type.bigResource
        )
    )
