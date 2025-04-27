package com.numplates.nomera3.modules.moments.settings.hidefrom.domain

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class MomentSettingsHideFromGetExclusionUseCase @Inject constructor(private val repository: MomentsRepository) {
    suspend fun invoke(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>> {
        return repository.getMomentsHideFromExclusions(limit, offset)
    }
}
