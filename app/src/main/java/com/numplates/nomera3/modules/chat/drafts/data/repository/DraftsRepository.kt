package com.numplates.nomera3.modules.chat.drafts.data.repository

import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel

interface DraftsRepository {

    suspend fun getAllDrafts(): List<DraftModel>

    suspend fun addDraft(draft: DraftModel)

    suspend fun deleteDraft(roomId: Long)

}
