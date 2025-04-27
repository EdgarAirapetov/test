package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import javax.inject.Inject

class UpdateCompanionNotificationsNetworkUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) : BaseUseCaseCoroutine<UpdateCompanionNotificationsNetworkParams, Boolean> {

    override suspend fun execute(
        params: UpdateCompanionNotificationsNetworkParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.updateDialogCompanionNotificationsNetwork(
            userId = params.userId,
            isMuted = params.isMuted,
            success = success,
            fail = fail
        )
    }

}

class UpdateCompanionNotificationsNetworkParams(
    val userId: Long,
    val isMuted: Boolean
) : DefParams()
