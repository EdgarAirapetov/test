package com.numplates.nomera3.modules.chat.drafts.domain

import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import javax.inject.Inject

class AddDraftUseCase @Inject constructor(
    private val repository: DraftsRepository
) {

    suspend fun invoke(draft: DraftModel) {
        repository.addDraft(draft)
    }

}
