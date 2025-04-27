package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomByCompanionFlowUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    fun invoke(companionId: String): Flow<DialogEntity?> = repository.getRoomByCompanionFlow(companionId)

    fun invoke(companionId: Long): Flow<DialogEntity?> = repository.getRoomByCompanionFlow(companionId)
}
