package com.numplates.nomera3.modules.moments.settings.notshow.domain

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class MomentSettingsNotShowAddExclusionUseCase @Inject constructor(private val repository: MomentsRepository) {
    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        return repository.addMomentNotShowExclusion(userIds)
    }
}
