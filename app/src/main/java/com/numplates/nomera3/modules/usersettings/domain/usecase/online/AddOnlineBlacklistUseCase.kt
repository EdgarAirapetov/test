package com.numplates.nomera3.modules.usersettings.domain.usecase.online

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class AddOnlineBlacklistUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        return api.addOnlineBlacklist(userIds)
    }
}
