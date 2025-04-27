package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class DeliveredUseCase(private val api: ApiMain) {

    fun markMessageAsDelivered(roomId: Long, messages: List<String>)
            = api.markAsDelivered(hashMapOf("room_id" to roomId, "ids" to messages))
}
