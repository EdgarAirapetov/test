package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import javax.inject.Inject

class GetUserBirthdayUseCase @Inject constructor(
    private val chatPersistRepository: ChatPersistRepository
) {
    fun invoke() = chatPersistRepository.getUserBirthday()
}
