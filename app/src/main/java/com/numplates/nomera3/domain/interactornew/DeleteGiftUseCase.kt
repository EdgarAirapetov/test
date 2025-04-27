package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper

class DeleteGiftUseCase(private val api: ApiMain) {

    suspend fun deleteGift(id: Long): ResponseWrapper<Any> {
        val body = hashMapOf("gift_id" to id)
        return api.deleteGift(body)
    }
}