package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import javax.inject.Inject

class UserPreferencesUseCase @Inject constructor(
    private val userRepository: UserRepository
) : BaseUseCaseCoroutine<UserProfileDefParams, Unit> {

    override suspend fun execute(
        params: UserProfileDefParams,
        success: (Unit) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        try {
            userRepository.pushUserSettingsChanged(params.state)
            success.invoke(Unit)

        } catch (e: Exception) {
            fail.invoke(e)
        }
    }


}

class UserProfileDefParams(
    val state: UserSettingsEffect
) : DefParams()
