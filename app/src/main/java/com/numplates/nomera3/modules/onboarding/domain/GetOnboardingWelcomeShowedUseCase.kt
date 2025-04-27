package com.numplates.nomera3.modules.onboarding.domain

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetOnboardingWelcomeShowedUseCase @Inject constructor(
    private val repository: UserRepository
){
    fun invoke() = repository.readOnboardingShowed()
}
