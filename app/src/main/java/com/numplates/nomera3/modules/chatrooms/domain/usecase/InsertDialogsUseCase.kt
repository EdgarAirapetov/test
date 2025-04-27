package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertDialogsUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    @Deprecated("Use suspend function below")
    fun invoke(newDialogs: List<DialogEntity>) =
         repository.insertDialogs(newDialogs)

    suspend fun invokeSuspend(rooms: List<DialogEntity>) = withContext(Dispatchers.IO) {
        repository.insertDialogs(rooms)
    }
}
