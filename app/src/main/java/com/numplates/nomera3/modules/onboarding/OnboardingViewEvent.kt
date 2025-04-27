package com.numplates.nomera3.modules.onboarding

import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel

/**
 * Класс, который будет содержать все возможные одноразовые эвенты для
 * [com.numplates.nomera3.modules.onboarding.OnboardingFragment]
 */
sealed class OnboardingViewEvent {
    object AuthFinished : OnboardingViewEvent()
    data class SendCodeSuccess(val sendTo: String, val timeout: Long?, val currentCountry: RegistrationCountryModel?) :
        OnboardingViewEvent()
    object StartRegistration: OnboardingViewEvent()

    sealed class Error : OnboardingViewEvent() {
        data class UserBlocked(val reason: String?, val blockExpired: Long?) : Error()
    }
}
