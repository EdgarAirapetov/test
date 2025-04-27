package com.numplates.nomera3.modules.usersettings.domain.usecase.map

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class AddMapWhitelistUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        return api.addMapWhitelist(userIds)
    }
}
