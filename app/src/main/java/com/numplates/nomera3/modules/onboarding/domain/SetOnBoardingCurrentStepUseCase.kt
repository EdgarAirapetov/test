package com.numplates.nomera3.modules.onboarding.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCase
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.onboarding.OnboardingStep
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepository
import javax.inject.Inject

class SetOnBoardingLastStep @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : BaseUseCase<OnBoardingStepDefParams, Unit>{

    override suspend fun execute(params: OnBoardingStepDefParams) {
        onboardingRepository.setLastStep(params.step)
    }
}

data class OnBoardingStepDefParams(val step: OnboardingStep) : DefParams()