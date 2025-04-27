package com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends

import com.meera.application_api.analytic.model.AmplitudeName

enum class AmplitudeMutualFriendsTypeName(
    private val event: String
) : AmplitudeName {

    /**
     * Отмечаем, когда пользователь изменил настройку видимость в профиле "Друзья и подписчики"
     */
    PRIVACY_AUDIENCE_VISIBILITY_CHANGE("privacy audience visibility change"),

    /**
     * Пользователь тапнул на друзей, подписчиков, подписки или общие подписки в чужом профиле
     */
    USER_AUDIENCE_TAP("user audience tap"),

    /**
     * Пользователь тапнул на некликабельный список друзей, подписчиков, подписки или общие подписки
     * в чужом профиле (когда списки скрыты или пусты)
     */
    UNCLICKABLE_USER_AUDIENCE_TAP("unclickable user audience tap");

    override val eventName: String
        get() = event
}
