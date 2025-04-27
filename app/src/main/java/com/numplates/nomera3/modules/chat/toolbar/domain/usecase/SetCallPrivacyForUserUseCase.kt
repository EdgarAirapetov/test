package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import javax.inject.Inject

class SetCallPrivacyForUserUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) : BaseUseCaseCoroutine<SetCallPrivacyForUserParams, Boolean> {

    override suspend fun execute(
        params: SetCallPrivacyForUserParams,
        success: (Boolean) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.setCallPrivacyForUser(
            userId = params.userId,
            isSet = params.isSet,
            success = success,
            fail = fail
        )
    }

}

class SetCallPrivacyForUserParams(
    val userId: Long,
    val isSet: Boolean,
) : DefParams()
