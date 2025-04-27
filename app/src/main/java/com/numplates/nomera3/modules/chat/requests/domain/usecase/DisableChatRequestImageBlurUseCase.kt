package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import javax.inject.Inject

class DisableChatRequestImageBlurUseCase @Inject constructor(
    private val repository: ChatRequestRepositoryImpl
) : BaseUseCaseCoroutine<DisableChatRequestImageBlurParams, Boolean> {

    override suspend fun execute(
        params: DisableChatRequestImageBlurParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.disableChatRequestImageBlur(params.message, success, fail)
    }

}

class DisableChatRequestImageBlurParams(
    val message: MessageEntity?
) : DefParams()
