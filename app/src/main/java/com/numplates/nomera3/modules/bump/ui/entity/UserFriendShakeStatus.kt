package com.numplates.nomera3.modules.bump.ui.entity

/**
 * Предполагаемые состояния шейка для ui:
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/3134128129
 */
enum class UserFriendShakeStatus {
    /**
     * Состояние, когда юзеру предлагается добавить в друзья другого юзера
     */
    USER_SHAKE_REQUEST_UNKNOWN,

    /**
     * В данном стейте пользователи уже друзья
     */
    USER_SHAKE_ALREADY_FRIENDS,

    /**
     * Запрос в друзья отправлен от меня
     */
    USER_SHAKE_FRIEND_REQUESTED_BY_ME,

    /**
     * Запрос в друзья отправлен другим юзером
     */
    USER_SHAKE_FRIEND_REQUESTED_BY_USER
}
