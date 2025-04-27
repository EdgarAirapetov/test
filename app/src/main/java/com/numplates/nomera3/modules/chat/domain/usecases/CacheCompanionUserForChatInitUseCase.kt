package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatRepository
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import javax.inject.Inject

class CacheCompanionUserForChatInitUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    fun invoke(user: ChatInitProfileData?): ChatInitProfileData? {
        return repository.cacheCompanionUserForChatInit(user)
    }

}
