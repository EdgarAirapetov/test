package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

/**
 * Данный listener используется, для слушателя изменение данных настройки чата.
 * Проблема в том, что socket при слабом интернете ничего не излучает и состояние не будет обновлено
 * Поэтому, мы будем отслеживать изменения через глобальный репозиторий.
 */
class UserChatPreferencesListener @Inject constructor(
    private val userRepository: UserRepository
) {

    fun subscribeChatState() = userRepository.getUserPrefObserver()
}