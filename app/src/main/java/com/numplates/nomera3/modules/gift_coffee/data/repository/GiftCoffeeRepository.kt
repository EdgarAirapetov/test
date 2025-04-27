package com.numplates.nomera3.modules.gift_coffee.data.repository

import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.GiftPlaceEntityResponse
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity

interface GiftCoffeeRepository {

    suspend fun markAsViewed(
            id: Long,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit)

    suspend fun getCoffeeCode(
            id: Long,
            type: CoffeeType,
            success: (PromoCodeEntity) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun getCoffeeAddress(
            query: String,
            limit: Int,
            offset: Int,
            success: (List<GiftPlaceEntityResponse>) -> Unit,
            fail: (Exception) -> Unit
    )
}
