package com.numplates.nomera3.modules.onboarding.data

import com.numplates.nomera3.modules.onboarding.OnboardingStep
import com.numplates.nomera3.modules.onboarding.OnboardingType

interface OnboardingRepository {

    suspend fun setLastStep(
        step: OnboardingStep
    )

    suspend fun getLastStep(
        success: (type: OnboardingStep) -> Unit,
        fail: (e: Exception) -> Unit
    )

    suspend fun getType(
        success: (type: OnboardingType) -> Unit,
        fail: (e: Exception) -> Unit
    )

    suspend fun setType(
        type: OnboardingType,
        success: (Unit) -> Unit,
        fail: (e: Exception) -> Unit
    )
}