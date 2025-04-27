package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class NotificationCounterUseCase(private val api: ApiMain ) {
    suspend fun getCounter() = api.getUnreadEventsCounter()
}