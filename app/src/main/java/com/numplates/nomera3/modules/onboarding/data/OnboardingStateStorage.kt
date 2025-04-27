package com.numplates.nomera3.modules.onboarding.data

import com.meera.core.preferences.PrefManager
import com.numplates.nomera3.modules.onboarding.OnboardingStep
import com.numplates.nomera3.modules.onboarding.OnboardingType

class OnboardingStateStorage(
    private val prefManager: PrefManager
) {

    var step: OnboardingStep
        get(){
            return getStepFromKey(prefManager.getString(KEY_STEP, OnboardingStep.STEP_JOIN.name))
        }
    set(value) {
        prefManager.putValue(KEY_STEP, value.name)
    }

    var type: OnboardingType
        get() {
            return getTypeFromKey(prefManager.getString(KEY_TYPE, OnboardingType.INITIAL.name))
        }
        set(value) {
            prefManager.putValue(KEY_TYPE, value.name)
        }

    private fun getStepFromKey(key: String?) : OnboardingStep {
        return when(key){
            OnboardingStep.STEP_JOIN_PHONE.name -> OnboardingStep.STEP_JOIN_PHONE
            OnboardingStep.STEP_JOIN.name -> OnboardingStep.STEP_JOIN
            OnboardingStep.STEP_CONNECT.name -> OnboardingStep.STEP_CONNECT
            OnboardingStep.STEP_EVENTS.name -> OnboardingStep.STEP_EVENTS
            OnboardingStep.STEP_PEOPLE_NEAR.name -> OnboardingStep.STEP_PEOPLE_NEAR
            else -> OnboardingStep.STEP_WELCOME
        }
    }

    private fun getTypeFromKey(key: String?): OnboardingType {
        return when (key) {
            OnboardingType.REGISTER_ON_START.name -> OnboardingType.REGISTER_ON_START
            OnboardingType.REGISTER_ON_END.name -> OnboardingType.REGISTER_ON_END
            else -> OnboardingType.INITIAL
        }
    }

    companion object {
        private const val KEY_STEP = "key step"
        private const val KEY_TYPE = "key type"
    }
}
