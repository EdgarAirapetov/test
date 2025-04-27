package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import javax.inject.Inject

class MuteNotificationsGroupChatUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) : BaseUseCaseCoroutine<MuteNotificationsGroupChatParams, Boolean> {

    override suspend fun execute(
        params: MuteNotificationsGroupChatParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.muteNotificationsGroupChat(params.roomId, success, fail)
    }

}

class MuteNotificationsGroupChatParams(
    val roomId: Long
) : DefParams()
