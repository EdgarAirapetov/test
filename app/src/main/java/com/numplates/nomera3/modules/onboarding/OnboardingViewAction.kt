package com.numplates.nomera3.modules.onboarding

sealed class OnboardingViewAction {
    object Setup: OnboardingViewAction()
    object StartRegistration: OnboardingViewAction()
}
