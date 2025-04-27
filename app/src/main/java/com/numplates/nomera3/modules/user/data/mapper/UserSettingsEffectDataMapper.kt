package com.numplates.nomera3.modules.user.data.mapper

import com.numplates.nomera3.modules.user.data.entity.UserSettingsDataEffect
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import javax.inject.Inject

class UserSettingsEffectDataMapper @Inject constructor() {

    fun mapFromDataEffect(effect: UserSettingsDataEffect): UserSettingsEffect {
        return when (effect) {
            is UserSettingsDataEffect.EnabledChatDataEffect ->
                UserSettingsEffect.EnabledChatEffect(effect.isChatEnabled)
            is UserSettingsDataEffect.UserBlockStatusChanged -> UserSettingsEffect.UserBlockStatusChanged(
                userId = effect.userId,
                isUserBlocked = effect.isUserBlocked
            )
            is UserSettingsDataEffect.UserFriendStatusChanged -> UserSettingsEffect.UserFriendStatusChanged(
                userId = effect.userId,
                isSubscribe = effect.isSubscribe
            )
            is UserSettingsDataEffect.SuggestionRemoved -> UserSettingsEffect.SuggestionRemoved(effect.userId)
        }
    }

    fun mapToDataEffect(effect: UserSettingsEffect): UserSettingsDataEffect = when (effect) {
        is UserSettingsEffect.EnabledChatEffect -> UserSettingsDataEffect.EnabledChatDataEffect(effect.isChatEnabled)
        is UserSettingsEffect.UserBlockStatusChanged -> UserSettingsDataEffect.UserBlockStatusChanged(
            userId = effect.userId,
            isUserBlocked = effect.isUserBlocked
        )
        is UserSettingsEffect.UserFriendStatusChanged -> UserSettingsDataEffect.UserFriendStatusChanged(
            effect.userId,
            effect.isSubscribe
        )
        is UserSettingsEffect.SuggestionRemoved -> UserSettingsDataEffect.SuggestionRemoved(effect.userId)
    }
}
