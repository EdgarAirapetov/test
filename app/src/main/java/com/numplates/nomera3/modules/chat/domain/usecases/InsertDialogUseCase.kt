package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class InsertDialogUseCase @Inject constructor(
    private val roomRepository: RoomDataRepository
){

    fun invoke(dialog: DialogEntity) =
        roomRepository.insertDialog(dialog)
}
