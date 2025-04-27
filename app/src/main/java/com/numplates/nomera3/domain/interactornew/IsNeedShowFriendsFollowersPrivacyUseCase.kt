package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class IsNeedShowFriendsFollowersPrivacyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun invoke(isNeedShow: Boolean) =
        userRepository.setUserFriendsPrivacyDialogShowed(isNeedShow)
}
