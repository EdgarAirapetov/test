package com.numplates.nomera3.modules.gift_coffee.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.gift_coffee.data.entity.GiftPlaceEntityResponse
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GiftCoffeeApi {

    //coffee_type: Enum(1 - cappuccino | 2 - latte | 3 - raf)
    @GET("/v2/purchases/get_coffee_code")
    suspend fun getCoffeeCode(
            @Query("gift_id") giftId: Long,
            @Query("coffee_type") coffeeType: Int
    ): ResponseWrapper<PromoCodeEntity>

    @POST("/v2/purchases/mark_as_viewed")
    suspend fun markAsViewed(
            @Query("gift_id") giftId: Long
    ): ResponseWrapper<Boolean>

    @GET("/v2/purchases/get_coffee_address")
    suspend fun getCoffeeAddress(
            @Query("query") query: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int
    ): ResponseWrapper<List<GiftPlaceEntityResponse>>
}
