package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class UpdateChatMembersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun invoke(members: List<ChatMember>) = userRepository.updateChatMembersDatabase(members)
}
