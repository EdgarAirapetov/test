package com.numplates.nomera3.modules.onboarding.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.onboarding.OnboardingStep
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepository
import javax.inject.Inject

class GetOnBoardingLastStepUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : BaseUseCaseCoroutine<DefParams, OnboardingStep>{

    override suspend fun execute(
        params: DefParams,
        success: (OnboardingStep) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        onboardingRepository.getLastStep(success, fail)
    }
}
