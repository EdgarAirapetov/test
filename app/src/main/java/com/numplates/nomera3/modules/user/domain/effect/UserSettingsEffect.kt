package com.numplates.nomera3.modules.user.domain.effect

sealed interface UserSettingsEffect {

    data class EnabledChatEffect(val isChatEnabled: Boolean) : UserSettingsEffect

    data class UserFriendStatusChanged(
        val userId: Long,
        val isSubscribe: Boolean = false
    ) : UserSettingsEffect

    data class UserBlockStatusChanged(val userId: Long, val isUserBlocked: Boolean) : UserSettingsEffect

    data class SuggestionRemoved(val userId: Long) : UserSettingsEffect
}
