package com.numplates.nomera3.modules.gift_coffee.data.entity

import androidx.annotation.DrawableRes
import com.numplates.nomera3.R

enum class CoffeeType(
        val value: Int,
        @Transient @DrawableRes val bigResource: Int,
        @Transient @DrawableRes val smallResource: Int
) {
    CAPPUCCINO(1, R.drawable.cappuccino_coffee_big, R.drawable.cappuccino_coffee_small),
    LATTE(2, R.drawable.latte_coffee_big, R.drawable.latte_coffee_small),
    RAF(3, R.drawable.raf_coffee_big, R.drawable.raf_coffee_small);
}
