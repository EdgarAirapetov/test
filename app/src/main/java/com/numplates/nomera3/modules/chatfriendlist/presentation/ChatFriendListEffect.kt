package com.numplates.nomera3.modules.chatfriendlist.presentation

sealed interface ChatFriendListEffect {
    data object OpenNewChatScreen: ChatFriendListEffect
}
