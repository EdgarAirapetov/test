package com.numplates.nomera3.modules.gift_coffee.data.repository

import com.numplates.nomera3.modules.gift_coffee.data.api.GiftCoffeeApi
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.GiftPlaceEntityResponse
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity
import javax.inject.Inject

class GiftCoffeeRepositoryImpl @Inject constructor(
        private val api: GiftCoffeeApi
) : GiftCoffeeRepository {

    override suspend fun markAsViewed(
            id: Long,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    ) = try {
        val result = api.markAsViewed(id).data
        if (result == null) fail(IllegalArgumentException("Empty result"))
        else success(result)
    } catch (e: Exception) {
        fail(e)
    }

    override suspend fun getCoffeeCode(id: Long,
                                       type: CoffeeType,
                                       success: (PromoCodeEntity) -> Unit,
                                       fail: (Exception) -> Unit) {
        try {
            val code = api.getCoffeeCode(id, type.value).data
            if (code == null) fail(IllegalArgumentException("Empty code"))
            else success(code)
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getCoffeeAddress(query: String,
                                          limit: Int,
                                          offset: Int,
                                          success: (List<GiftPlaceEntityResponse>) -> Unit,
                                          fail: (Exception) -> Unit) {
        try {
            val result = api.getCoffeeAddress(query, limit, offset).data
            if (result == null) fail(IllegalArgumentException("Empty result"))
            else success(result)
        } catch (e: Exception) {
            fail(e)
        }
    }
}
