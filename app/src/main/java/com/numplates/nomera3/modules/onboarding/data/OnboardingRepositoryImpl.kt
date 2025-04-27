package com.numplates.nomera3.modules.onboarding.data

import com.numplates.nomera3.modules.onboarding.OnboardingStep
import com.numplates.nomera3.modules.onboarding.OnboardingType
import timber.log.Timber
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingStateStorage: OnboardingStateStorage
): OnboardingRepository {

    override suspend fun setLastStep(
        step: OnboardingStep,
    ) {
        try {
            onboardingStateStorage.step = step
        }catch (e: java.lang.Exception){
            Timber.e(e)
        }
    }

    override suspend fun getLastStep(
        success: (type: OnboardingStep) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            success(onboardingStateStorage.step)
        }catch (e: Exception){
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun getType(
        success: (type: OnboardingType) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            success(onboardingStateStorage.type)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun setType(
        type: OnboardingType,
        success: (Unit) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            onboardingStateStorage.type = type
            success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }
}