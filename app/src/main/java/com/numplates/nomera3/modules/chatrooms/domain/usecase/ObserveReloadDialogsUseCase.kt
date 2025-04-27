package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReloadDialogsUseCase @Inject constructor(
    private val repository: RoomDataRepositoryImpl
) {

    fun invoke(): Flow<Boolean> {
        return repository.observeReloadDialogs()
    }

}
