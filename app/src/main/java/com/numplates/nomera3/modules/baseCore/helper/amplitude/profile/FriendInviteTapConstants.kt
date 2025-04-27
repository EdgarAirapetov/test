package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

private const val WHERE = "where" // Отмечаем, откуда пользователь перешел на экран приглашения друзей

enum class FriendInviteTapEventName(
    private val event: String
) : AmplitudeName {
    FRIEND_INVITE_TAP("friend invite tap");

    override val eventName: String
        get() = event
}

enum class FriendInviteTapProperty(
    private val property: String
) : AmplitudeProperty {
    MAIN_FEED("main feed"),
    SETTINGS("settings"),
    NAVBAR("navbar"),
    DEEPLINK("deeplink"),
    PUSH("push"),
    PEOPLE("people"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = WHERE
}
