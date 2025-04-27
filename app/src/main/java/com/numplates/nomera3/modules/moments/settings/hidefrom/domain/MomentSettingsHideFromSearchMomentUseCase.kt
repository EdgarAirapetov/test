package com.numplates.nomera3.modules.moments.settings.hidefrom.domain

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class MomentSettingsHideFromSearchMomentUseCase @Inject constructor(private val repository: MomentsRepository) {
    suspend fun invoke(
        name: String,
        limit: Int,
        offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>> {
        return repository.searchHideFromExclusion(name, limit, offset)
    }
}
