package com.numplates.nomera3.modules.onboarding.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.onboarding.OnboardingType
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepository
import javax.inject.Inject

class GetOnboardingTypeUseCase @Inject constructor(
    private val repository: OnboardingRepository
): BaseUseCaseCoroutine<DefParams, OnboardingType> {
    override suspend fun execute(
        params: DefParams,
        success: (OnboardingType) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getType(success, fail)
    }
}
