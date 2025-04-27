package com.numplates.nomera3.modules.usersettings.domain.usecase.call

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class AddCallBlacklistUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        return api.addCallUserToBlackList(userIds)
    }
}
