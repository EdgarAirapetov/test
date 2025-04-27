package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepository
import com.numplates.nomera3.modules.userprofile.data.entity.GreetingModel
import com.numplates.nomera3.modules.userprofile.data.entity.toGreetingModel
import javax.inject.Inject

class SendGreetingUseCase @Inject constructor(
    private val messagesRepository: MessagesRepository
) : BaseUseCaseCoroutine<SendGreetingUseCaseParams, GreetingModel> {

    override suspend fun execute(
        params: SendGreetingUseCaseParams,
        success: (GreetingModel) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        messagesRepository.sendGreeting(
            params.userId,
            params.stickerId,
            { greetingResponse -> success.invoke(greetingResponse.toGreetingModel()) },
            fail
        )
    }
}

class SendGreetingUseCaseParams(
    val userId: Long,
    val stickerId: Int?
) : DefParams()
