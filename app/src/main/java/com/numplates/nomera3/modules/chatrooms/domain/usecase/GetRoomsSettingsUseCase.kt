package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.entity.RoomsSettingsModel
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomsSettingsUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    fun invoke(): RoomsSettingsModel? {
        return repository.getRoomsSettings()
    }

}
