package com.numplates.nomera3.modules.onboarding

import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel

sealed class OnboardingViewState {
    data class Setup(val currentStep: OnboardingStep, val totalSteps: List<OnboardingStep>): OnboardingViewState()
    data class SetContinueButtonAvailable(val available: Boolean) : OnboardingViewState()
    data class CurrentCountry(val country: RegistrationCountryModel) : OnboardingViewState()
}
