package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class UpdateDialogUseCase @Inject constructor(
    private val roomDataRepository: RoomDataRepository
) {

    suspend fun invoke(dialogEntity: DialogEntity) {
        roomDataRepository.updateDialog(dialogEntity)
    }

}
