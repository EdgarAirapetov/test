package com.numplates.nomera3.modules.chat.drafts.domain

import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import javax.inject.Inject

class GetAllDraftsUseCase @Inject constructor(
    private val repository: DraftsRepository
) {

    suspend fun invoke(): List<DraftModel> {
        return repository.getAllDrafts()
    }

}
