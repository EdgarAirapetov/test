package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class ClearChatMembersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun invoke() = userRepository.clearChatMembers()
}
