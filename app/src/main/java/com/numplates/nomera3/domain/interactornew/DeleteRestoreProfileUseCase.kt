package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.chat.drafts.domain.DraftsRepository

class DeleteRestoreProfileUseCase(
    private val api: ApiMain,
    private val draftsRepository: DraftsRepository
) {

    suspend fun deleteOwnProfile(reasonId: Int?): ResponseWrapper<Any> {
        draftsRepository.deleteAllDrafts()
        return api.deleteOwnProfile(hashMapOf("reason_id" to reasonId))
    }

    suspend fun restoreOwnProfile() = api.restoreOwnProfile()
}
