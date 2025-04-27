package com.numplates.nomera3.modules.chatfriendlist.presentation

sealed class ChatFriendListViewEvent {
    object Empty: ChatFriendListViewEvent()
    object FriendList: ChatFriendListViewEvent()
    object NoFriends: ChatFriendListViewEvent()
    object FailedToLoadFriendList: ChatFriendListViewEvent()
    object FailedToLoadFriendProfile: ChatFriendListViewEvent()
    object EmptySearchResult: ChatFriendListViewEvent()
    object OpenNewChatScreen: ChatFriendListViewEvent()
    data class ChangeNewGroupChatButtonVisibility(val isVisible: Boolean): ChatFriendListViewEvent()
}
