package com.numplates.nomera3.modules.chat.drafts.domain

import javax.inject.Inject

class DeleteDraftUseCase @Inject constructor(
    private val repository: DraftsRepository
) {

    suspend fun invoke(roomId: Long?) {
        repository.deleteDraft(roomId)
    }

}
