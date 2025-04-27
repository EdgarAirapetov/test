package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.NotificationResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

/**
 * User notifications
 */
class GetAllEventsUseCase(private val repository: ApiHiWay) {

    // default - limit:20, offset:0
    fun getAllEvents(userId: Long, limit: Int, offset: Int): Flowable<ResponseWrapper<MutableList<NotificationResponse>>> =
            repository.getAllEvents(userId, limit, offset)

}