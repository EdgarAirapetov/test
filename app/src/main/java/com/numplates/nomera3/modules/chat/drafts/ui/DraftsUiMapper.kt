package com.numplates.nomera3.modules.chat.drafts.ui

import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import com.meera.db.models.DraftUiModel
import javax.inject.Inject

class DraftsUiMapper @Inject constructor() {

    fun mapDomainToUiModel(src: DraftModel): DraftUiModel {
        return DraftUiModel(
            roomId = src.roomId,
            userId = src.userId,
            lastUpdatedTimestamp = src.lastUpdatedTimestamp,
            text = src.text,
            reply = src.reply,
            draftId = src.draftId
        )
    }

}
