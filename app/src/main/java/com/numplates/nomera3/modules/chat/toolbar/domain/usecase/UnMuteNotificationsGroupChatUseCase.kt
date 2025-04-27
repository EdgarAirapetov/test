package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import javax.inject.Inject

class UnMuteNotificationsGroupChatUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) : BaseUseCaseCoroutine<UnMuteNotificationsGroupChatParams, Boolean> {

    override suspend fun execute(
        params: UnMuteNotificationsGroupChatParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.unmuteNotificationsGroupChat(params.roomId, success, fail)
    }

}

class UnMuteNotificationsGroupChatParams(
    val roomId: Long
) : DefParams()
