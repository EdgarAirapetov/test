package com.numplates.nomera3.modules.purchase.data.api

import com.numplates.nomera3.data.network.IapRequest
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.purchase.data.model.GiftCategoriesDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface PurchaseApi {

    @Headers("X-Skip-Cache: 1")
    @POST("/v1/iap/android")
    suspend fun purchaseProduct(@Body request: IapRequest?): ResponseWrapper<Boolean>

    @Headers("X-Skip-Cache: 1")
    @GET("/purchases/get_gifts")
    suspend fun getGifts(): ResponseWrapper<GiftCategoriesDto>
}
