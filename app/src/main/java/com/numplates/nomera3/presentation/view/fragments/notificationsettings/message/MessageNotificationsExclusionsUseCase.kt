package com.numplates.nomera3.presentation.view.fragments.notificationsettings.message

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper

class MessageNotificationsExclusionsUseCase(private val endpoints: ApiMain) {

    suspend fun getMessageSettingsExclusion(limit: Int, offset: Int): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        val params = hashMapOf(
                "limit" to limit,
                "offset" to offset
        )
        return endpoints.getMessageSettingsExclusion(params)
    }

    suspend fun searchMessageSettingsExclusion(name: String, limit: Int, offset: Int) =
            endpoints.searchMessageSettingsExclusion(name, limit, offset)


    suspend fun addMessageSettingsExclusion(userIds: List<Long>) =
            endpoints.addMessageSettingsExclusion(userIds)

    suspend fun deleteMessageSettingsExclusion(userIds: List<Long>?): ResponseWrapper<Any> {

        val body = if(userIds != null)
            hashMapOf("ids" to userIds)
        else hashMapOf()
        return endpoints.deleteMessageSettingsExclusion(body)
    }

}
