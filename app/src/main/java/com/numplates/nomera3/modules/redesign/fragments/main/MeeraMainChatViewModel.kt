package com.numplates.nomera3.modules.redesign.fragments.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.meera.core.extensions.combineWith
import com.numplates.nomera3.modules.chat.requests.domain.usecase.ChatRequestInfoUseCase
import com.numplates.nomera3.modules.chat.requests.ui.ChatRequestDataUiModel
import com.numplates.nomera3.modules.chatrooms.domain.usecase.ObserveUnreadMessageCountUseCase
import javax.inject.Inject

class MeeraMainChatViewModel @Inject constructor(
    private val unreadMessagesUseCase: ObserveUnreadMessageCountUseCase,
    private val chatRequestInfoUseCase: ChatRequestInfoUseCase,
): ViewModel() {

    private var liveUnreadMessages: LiveData<Int?> = unreadMessagesUseCase.invoke()


    private var liveChatRequest: LiveData<ChatRequestDataUiModel?> = chatRequestInfoUseCase.invoke()

    fun getTabLayoutUiState(): LiveData<TabLayoutUiState?> {
        val liveUnreadMessages = liveUnreadMessages.distinctUntilChanged()
        val liveChatRequest = liveChatRequest.distinctUntilChanged()
        return liveUnreadMessages.combineWith(liveChatRequest) { unreadMessages, chatRequest  ->
            if (unreadMessages != null && chatRequest != null) {
                val requestRoomCount = chatRequest.totalRoomsCount ?: 0
                val requestUnreadMessageCount = chatRequest.unreadMessageCount ?: 0
                return@combineWith TabLayoutUiState(
                    unreadMessageCount = unreadMessages,
                    isTabRequestVisible = requestRoomCount > 0,
                    requestCounter = requestUnreadMessageCount
                )
            }
            null
        }
    }

}
