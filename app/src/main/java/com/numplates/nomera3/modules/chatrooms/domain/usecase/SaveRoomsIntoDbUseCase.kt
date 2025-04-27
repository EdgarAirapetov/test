package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class SaveRoomsIntoDbUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    fun invoke(dialogs: List<DialogEntity>, drafts: List<DraftModel>, unsentRooms: Set<Long>) =
        repository.saveRoomsIntoDb(dialogs = dialogs, drafts = drafts, unsentRooms = unsentRooms)
}
