package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class SubscribersUseCase(private val api: ApiMain) {

    suspend fun getUserSubscribers(userId: Long, limit: Int, offset: Int)
            = api.getUserSubscribers(userId, limit, offset, "UserSimple")

    suspend fun deleteUserFromSubscribers(ids: List<Long>) =
            api.deleteUserFromSubscribers(
                    hashMapOf("ids" to ids)
            )

    suspend fun subscriberSearch(userId: Long, limit: Int, offset: Int, name: String)
            = api.subscriberSearch(userId, limit, offset,"UserSimple" ,name)

}