package com.numplates.nomera3.modules.user.data.entity

/**
 * Данный state будет хранить все возможные состояния, когда юзер поменял настройки приватности
 */
sealed class UserSettingsDataEffect {
    /**
     * Хранит состояние, если юзер поменял состояние чат:
     * 1: "Запретить сообщения"
     * 2: "Разрешить сообщения"
     */
    class EnabledChatDataEffect(
        val isChatEnabled: Boolean
    ) : UserSettingsDataEffect()

    /**
     * Данный event должен отработать тогда, когда юзер в чужом профиле меняет состояние "Дружбы".
     * И когда юзер меняет состояние "Дружбы" в пределах viewModel
     * [com.numplates.nomera3.presentation.viewmodel.UserSubscriptionsFriendsInfoViewModel]
     */
    class UserFriendStatusChanged(
        val userId: Long,
        val isSubscribe: Boolean = false
    ) : UserSettingsDataEffect()

    /**
     * Данный event отработает тогда, когда юзер заблокировал/разблокировал другого юзера
     */
    class UserBlockStatusChanged(val userId: Long, val isUserBlocked: Boolean) : UserSettingsDataEffect()

    data class SuggestionRemoved(val userId: Long) : UserSettingsDataEffect()
}
