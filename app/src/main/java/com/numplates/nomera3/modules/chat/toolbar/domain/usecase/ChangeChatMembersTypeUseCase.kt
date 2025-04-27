package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class ChangeChatMembersTypeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun invoke(membersIds: List<Long>, type: String) =
        userRepository.changeChatMembersType(membersIds, type)
}
