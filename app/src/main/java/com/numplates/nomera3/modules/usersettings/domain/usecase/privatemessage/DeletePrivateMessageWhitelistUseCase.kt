package com.numplates.nomera3.modules.usersettings.domain.usecase.privatemessage

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class DeletePrivateMessageWhitelistUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        val body = hashMapOf("ids" to userIds)
        return api.deletePrivateMessagesUserFromWhiteList(body)
    }
}
