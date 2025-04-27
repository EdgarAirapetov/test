package com.numplates.nomera3.domain.interactornew

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper

class CallUserSettingsUseCase(private val endpoints: ApiMain) {

    suspend fun searchCallUsersBlackList(name: String, limit: Int, offset: Int) =
            endpoints.searchCallUsersBlackList(name, limit, offset)

    suspend fun getCallUsersBlackList(limit: Int, offset: Int): ResponseWrapper<UsersWrapper<UserSimple>> {
        val params = hashMapOf(
                "limit" to limit,
                "offset" to offset
        )
        return endpoints.getCallUsersBlackList(params)
    }

    suspend fun addCallUserBlackList(userIds: List<Long>) =
            endpoints.addCallUserToBlackList(userIds)

    suspend fun deleteCallUserBlackList(userIds: List<Long>): ResponseWrapper<Any> {
        val body = hashMapOf("ids" to userIds)
        return endpoints.deleteCallUserFromBlackList(body)
    }


    suspend fun searchCallUsersWhiteList(name: String, limit: Int, offset: Int) =
            endpoints.searchCallUsersWhiteList(name, limit, offset)

    suspend fun getCallUsersWhiteList(limit: Int, offset: Int): ResponseWrapper<UsersWrapper<UserSimple>> {
        val params = hashMapOf(
                "limit" to limit,
                "offset" to offset
        )
        return endpoints.getCallUsersWhiteList(params)
    }

    suspend fun addCallUserWhiteList(userIds: List<Long>) =
            endpoints.addCallUserToWhiteList(userIds)

    suspend fun deleteCallUserWhiteList(userIds: List<Long>): ResponseWrapper<Any> {
        val body = hashMapOf("ids" to userIds)
        return endpoints.deleteCallUserFromWhiteList(body)
    }

}
