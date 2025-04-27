package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import javax.inject.Inject

class UpdateCompanionUserDbUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) : BaseUseCaseCoroutine<UpdateCompanionUserDbParams, Boolean> {

    override suspend fun execute(
        params: UpdateCompanionUserDbParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.updateCompanionUser(
            roomId = params.roomId,
            user = params.companion,
            success = success,
            fail = fail
        )
    }

}

class UpdateCompanionUserDbParams(
    val roomId: Long,
    val companion: UserChat
) : DefParams()
