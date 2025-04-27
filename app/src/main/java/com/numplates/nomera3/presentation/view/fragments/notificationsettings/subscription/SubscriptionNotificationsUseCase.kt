package com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper

class SubscriptionNotificationsUseCase(private val api: ApiMain) {

    suspend fun searchUsers(name: String, limit: Int, offset: Int) =
            api.searchSubscriptionsNotificationsUsers(name, limit, offset, "UserSimple")


    suspend fun getUsers(limit: Int, offset: Int) =
            api.getSubscriptionsNotificationsUsers(limit, offset, "UserSimple")

    suspend fun addUsers(userIds: List<Long>) =
            api.addSubscriptionsNotificationsUser(userIds)

    suspend fun deleteUsers(userIds: List<Long>): ResponseWrapper<Any> {
        val body = hashMapOf("ids" to userIds)
        return api.deleteSubscriptionsNotificationsUser(body)
    }


}