package com.numplates.nomera3.modules.moments.settings.hidefrom.domain

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

private const val USER_IDS_KEY = "ids"

class MomentSettingsHideFromDeleteExclusionUseCase @Inject constructor(private val repository: MomentsRepository) {
    suspend fun invoke(userIds: List<Long>): ResponseWrapper<Any> {
        val body = hashMapOf(USER_IDS_KEY to userIds)
        return repository.deleteMomentHideFromExclusion(body)
    }
}
