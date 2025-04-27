package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class InsertSettingsUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    fun invoke() = repository.insertSettingsDao()
}
