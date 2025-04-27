package com.numplates.nomera3.modules.chatrooms.domain.usecase

import androidx.lifecycle.LiveData
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class ObserveUnreadMessageCountUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    fun invoke(): LiveData<Int?> = repository.observeUnreadMessageCount()

}
