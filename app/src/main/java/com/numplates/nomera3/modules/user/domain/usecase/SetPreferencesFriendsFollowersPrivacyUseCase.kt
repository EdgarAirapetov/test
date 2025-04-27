package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class SetPreferencesFriendsFollowersPrivacyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun invoke(key: Int) = userRepository.setPrivacyFriendsFollowers(key)
}