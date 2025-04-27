package com.numplates.nomera3.modules.usersettings.domain.usecase.map

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class SearchMapWhitelistUseCase @Inject constructor(private val api: ApiMain) {

    suspend fun invoke(
        name: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return api.searchMapWhitelist(name, limit, offset)
    }
}
