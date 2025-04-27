package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetFriendsSubscribersPopupPrivacyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun invoke() = userRepository.getUserNeedShowFriendsSubscribersPopup()
}