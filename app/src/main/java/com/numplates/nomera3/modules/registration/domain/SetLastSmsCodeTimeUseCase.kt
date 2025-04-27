package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class SetLastSmsCodeTimeUseCase @Inject constructor(
    private val registrationRepository: UserRepository
) {
    fun invoke() = registrationRepository.saveLastSmsCodeTime()
}
