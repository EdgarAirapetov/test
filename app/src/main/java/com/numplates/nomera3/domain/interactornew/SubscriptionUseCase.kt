package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository

class SubscriptionUseCase(val api: ApiMain, val momentsRepository: MomentsRepository) {

    suspend fun getUserSubscriptions(userId: Long, limit: Int, offset: Int) =
        api.getUserSubscriptions(userId = userId, limit = limit, offset = offset, userType = "UserSimple")

    suspend fun deleteFromSubscriptions(ids: List<Long>): ResponseWrapper<Any> {
        val response = api.deleteUserFromSubscriptions(hashMapOf("ids" to ids))
        if (response.data != null) momentsRepository.updateUserSubscriptions(userIds = ids, isAdded = false)
        return response
    }

    suspend fun subscriptionsSearch(userId: Long, limit: Int, offset: Int, name: String) =
        api.subscriptionsSearch(userId = userId, limit = limit, offset = offset, userType = "", name = name)

    suspend fun addSubscription(ids: List<Long>): ResponseWrapper<Any> {
        val response = api.addSubscriptions(hashMapOf("ids" to ids))
        if (response.data != null) momentsRepository.updateUserSubscriptions(userIds = ids, isAdded = true)
        return response
    }
}
