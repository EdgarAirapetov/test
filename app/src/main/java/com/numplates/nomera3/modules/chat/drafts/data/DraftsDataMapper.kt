package com.numplates.nomera3.modules.chat.drafts.data

import com.meera.db.models.DraftDbModel
import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import javax.inject.Inject

class DraftsDataMapper @Inject constructor() {

    fun mapDomainToDbModel(src: DraftModel): DraftDbModel {
        return DraftDbModel(
            roomId = src.roomId,
            userId = src.userId,
            lastUpdatedTimestamp = src.lastUpdatedTimestamp,
            text = src.text,
            reply = src.reply
        ).apply {
            src.draftId?.let { draftId = it }
        }
    }

    fun mapDbToDomainModel(src: DraftDbModel): DraftModel {
        return DraftModel(
            roomId = src.roomId,
            userId = src.userId,
            lastUpdatedTimestamp = src.lastUpdatedTimestamp,
            text = src.text,
            reply = src.reply,
            draftId = src.draftId
        )
    }

}
