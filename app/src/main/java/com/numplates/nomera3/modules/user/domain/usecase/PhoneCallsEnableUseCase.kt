package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class PhoneCallsEnableUseCase @Inject constructor(
        private val userRepository: UserRepository,
) {

    suspend fun invoke(userId: Long) {
        userRepository.setUserCallPrivacy(userId = userId, isSet = true)
    }
}