package com.numplates.nomera3.presentation.model.enums

enum class ChatEventEnum(val state: Int) {
    NEW_GROUP_ROOM(0), // Создана новая комната (груповой чат) с твоим участием
    NEW_GROUP_ROOM_ADDED(1), // Ты был добавлен в груповой чат (уже существующий)
    REMOVED_FROM_GROUP_CHAT(2), // Ты был удален из групового чата
    ADDED_ADMINS(3),
    REMOVED_ADMIN(4),
    CHANGED_TITLE(5),
    CHANGED_DESCRIPTION(6),
    ROOM_DELETED(7),
    ROOM_DELETED_FOR_USER(8),
    CHANGED_AVATAR(9),
    LEAVE_ROOM(11),

    TEXT(99),
    IMAGE(10),
    GIF(11),
    AUDIO(12),
    VIDEO(13),
    REPOST(14),
    LIST(15),
    GIFT(16),
    SHARE_PROFILE(17),
    SHARE_COMMUNITY(18),
    GREETING(19),
    MOMENT(20),
    STICKER(21),
    OTHER(99),
    CALL(100);

    companion object {
        private val map = entries.associateBy { it.state }
        operator fun get(value: Int) = map[value]
    }
}
