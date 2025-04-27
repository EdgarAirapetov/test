package com.numplates.nomera3.modules.onboarding.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.onboarding.OnboardingType
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepository
import javax.inject.Inject

class SetOnboardingTypeUseCase @Inject constructor(
    private val repository: OnboardingRepository
): BaseUseCaseCoroutine<SetOnboardingTypeParams, Unit> {
    override suspend fun execute(
        params: SetOnboardingTypeParams,
        success: (Unit) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.setType(
            type = params.type,
            success = success,
            fail = fail
            )

    }
}

data class SetOnboardingTypeParams(val type: OnboardingType): DefParams()
