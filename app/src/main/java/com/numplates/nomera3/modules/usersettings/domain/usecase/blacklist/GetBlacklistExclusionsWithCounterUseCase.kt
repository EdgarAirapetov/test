package com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class GetBlacklistExclusionsWithCounterUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return api.getBlacklistExclusionsWithCounter(limit, offset)
    }
}
